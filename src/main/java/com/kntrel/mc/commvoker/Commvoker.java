package com.kntrel.mc.commvoker;

import com.kntrel.mc.commvoker.annotation.Command;
import com.kntrel.mc.commvoker.argument.ArgumentTypeRegistry;
import com.kntrel.mc.commvoker.argument.ArgumentTypeResolver;
import com.kntrel.mc.commvoker.exception.BadCommandClassException;
import com.kntrel.mc.commvoker.exception.BadCommandMethodException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.plugin.Plugin;
import java.lang.reflect.Method;
import java.util.*;

public class Commvoker {

    private final Plugin plugin_;
    private final ArgumentTypeResolver argumentTypeResolver_;
    private final CommandDispatcher<CommandSourceStack> dispatcher_;
    private final Set<Class<?>> instanceClasses_;


    public Commvoker(Plugin plugin) {
        this.plugin_ = plugin;
        this.argumentTypeResolver_ = new ArgumentTypeResolver();
        this.dispatcher_ = ReflectiveDispatcherAccessor.getDispatcher(this.plugin_.getServer());
        this.instanceClasses_ = new HashSet<>();
    }


    public void register(Object src) {
        if (this.instanceClasses_.contains(src.getClass())) {
            throw new IllegalStateException("An instance of '" + src.getClass().getName() + "' has already been registered into this Commvoker");
        }

        Class<?> clazz = src.getClass();
        List<Method> commandMethods = Arrays.stream(clazz.getMethods())
                .filter(m -> m.isAnnotationPresent(Command.class))
                .toList();

        Command outerAnnotation = clazz.getAnnotation(Command.class);
        boolean nested = outerAnnotation != null && !outerAnnotation.value().isEmpty();
        String outerLabel = nested ? outerAnnotation.value() : null;

        if (nested && !CommandParser.checkValidLiteral(outerLabel)) {
            throw new BadCommandClassException(src, "Bad @Command literal declaration");
        }

        for (Method m : commandMethods) {
            String raw = m.getAnnotation(Command.class).value();
            CommandParser.Token[] tokens;
            try {
                tokens = CommandParser.parse(this.argumentTypeResolver_, raw, m);
            } catch (BadCommandMethodException e) {
                throw new BadCommandClassException(src, e);
            }
            if (!nested && tokens.length < 1) {
                throw new BadCommandClassException(src, new BadCommandMethodException(m, raw, 0, "Empty command"));
            }
            if (!nested && !(tokens[0] instanceof CommandParser.Literal)) {
                throw new BadCommandClassException(src, new BadCommandMethodException(m, raw, 0, "First word of a command must be a literal, unless it's in a @Command annotated class"));
            }

            List<String> methodArgs = new ArrayList<>();
            LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(nested ? outerLabel : tokens[0].label());
            ArgumentBuilder<CommandSourceStack, ?> upper = root;
            for (int i = nested ? 0 : 1; i < tokens.length; i++) {
                CommandParser.Token t = tokens[i];
                ArgumentBuilder<CommandSourceStack, ?> arg;
                if (t instanceof CommandParser.Argument a) {
                    arg = RequiredArgumentBuilder.argument(a.label(), a.argumentType());
                    methodArgs.add(a.label());
                } else {
                    arg = LiteralArgumentBuilder.literal(t.label());
                }
                upper.then(arg);
                upper = arg;
            }
            upper.executes(new CommandMethodInvoker<>(src, m, methodArgs.toArray(new String[0])));

            this.dispatcher_.register(root);
        }

        this.instanceClasses_.add(src.getClass());
    }

    public ArgumentTypeRegistry getArgumentTypeRegistry() {
        return this.argumentTypeResolver_;
    }

}
