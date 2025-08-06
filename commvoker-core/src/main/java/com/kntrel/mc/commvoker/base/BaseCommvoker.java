package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.annotation.Command;
import com.kntrel.mc.commvoker.argument.ArgumentRegistry;
import com.kntrel.mc.commvoker.provided.ArgumentBindings;
import com.kntrel.mc.commvoker.command.CommandPatternToken;
import com.kntrel.mc.commvoker.exception.BadCommandClassException;
import com.kntrel.mc.commvoker.exception.BadCommandMethodException;
import com.kntrel.mc.commvoker.exception.BadCommandTokenException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class BaseCommvoker<S> {

    private final ArgumentResolverImpl<S> argumentResolver_;
    private final CommandParser<S> commandParser_;
    private final CommandDispatcher<S> dispatcher_;
    private final Set<Class<?>> instanceClasses_;


    public BaseCommvoker(CommandDispatcher<S> commandDispatcher) {
        this.dispatcher_ = commandDispatcher;
        this.argumentResolver_ = new ArgumentResolverImpl<>();
        this.commandParser_ = new CommandParser<>(this.argumentResolver_);
        this.instanceClasses_ = new HashSet<>();

        ArgumentBindings.all().forEach(this.argumentResolver_::register);
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
        boolean nested = outerAnnotation != null;
        CommandPatternToken[] outerTokens = new CommandPatternToken[0];

        if (nested) try {
            if (outerAnnotation.value().isEmpty()) {
                outerTokens = this.commandParser_.tokenize(Utils.toSnakeCase(clazz.getSimpleName()));
            } else {
                outerTokens = this.commandParser_.tokenize(outerAnnotation.value());
                if (outerTokens.length < 1 || !outerTokens[0].isLiteral()) {
                    outerTokens = Utils.arrayJoin(this.commandParser_.tokenize(Utils.toSnakeCase(clazz.getSimpleName())), outerTokens);
                }
            }
        } catch (BadCommandTokenException e) {
            throw new BadCommandClassException(src, e);
        }

        for (Method m : commandMethods) {
            Command annotation = m.getAnnotation(Command.class);
            String raw = annotation.value();
            CommandPatternToken[] tokens;
            try {
                tokens = this.commandParser_.tokenize(raw);
            } catch (BadCommandTokenException e) {
                throw new BadCommandClassException(src, e);
            }
            if (tokens.length < 1 || !tokens[0].isLiteral()) {
                if (annotation.extend() && !nested) {
                    throw new BadCommandClassException(src, new BadCommandMethodException(m, "'extend = true' command methods are only valid inside @Command annotated classes"));
                }
                if (!annotation.extend()) {
                    tokens = Utils.arrayJoin(
                            new CommandPatternToken[] { CommandPatternToken.literal(Utils.toSnakeCase(m.getName())) },
                            tokens
                    );
                }
            }
            if (nested) {
                tokens = Utils.arrayJoin(outerTokens, tokens);
            }

            LiteralArgumentBuilder<S> commandTree;
            try {
                commandTree = this.commandParser_.brigadierCommand(tokens, m, src);
            } catch (BadCommandMethodException e) {
                throw new BadCommandClassException(src, e);
            }

            this.register(commandTree);
        }

        this.instanceClasses_.add(src.getClass());
    }

    public void register(LiteralArgumentBuilder<S> tree) {
        this.dispatcher_.register(tree);
    }

    public int execute(String command, S src) throws CommandSyntaxException {
        return this.dispatcher_.execute(command, src);
    }

    public ArgumentRegistry<S> getArgumentRegistry() {
        return this.argumentResolver_;
    }

    protected CommandDispatcher<S> getCommandDispatcher() {
        return this.dispatcher_;
    }
}