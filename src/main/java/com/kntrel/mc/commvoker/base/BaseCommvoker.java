package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.annotation.Command;
import com.kntrel.mc.commvoker.argument.ArgumentTypeRegistry;
import com.kntrel.mc.commvoker.exception.BadCommandClassException;
import com.kntrel.mc.commvoker.exception.BadCommandMethodException;
import com.kntrel.mc.commvoker.exception.BadCommandTokenException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.lang.reflect.Method;
import java.util.*;

public abstract class BaseCommvoker<T> {

    private final ArgumentTypeResolver argumentTypeResolver_;
    private final CommandParser commandParser_;
    private final CommandDispatcher<T> dispatcher_;
    private final Set<Class<?>> instanceClasses_;


    public BaseCommvoker(CommandDispatcher<T> commandDispatcher) {
        this.dispatcher_ = commandDispatcher;
        this.argumentTypeResolver_ = new ArgumentTypeResolver();
        this.commandParser_ = new CommandParser(this.argumentTypeResolver_);
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
        CommandParser.Token[] outerTokens = new CommandParser.Token[0];

        if (nested) {
            try {
                outerTokens = this.commandParser_.tokenize(outerAnnotation.value());
            } catch (BadCommandTokenException e) {
                throw new BadCommandClassException(src, e);
            }
            if (outerTokens.length < 1) {
                nested = false;
            } else if (!outerTokens[0].type().equals(CommandParser.TokenType.LITERAL)) {
                throw new BadCommandClassException(src, "Firs argument of a @Command annotated class must be a literal.");
            }
        }

        for (Method m : commandMethods) {
            Command annotation = m.getAnnotation(Command.class);
            String raw = annotation.value();
            CommandParser.Token[] tokens;
            try {
                tokens = this.commandParser_.tokenize(raw);
            } catch (BadCommandTokenException e) {
                throw new BadCommandClassException(src, e);
            }
            if (tokens.length < 1 || !tokens[0].type().equals(CommandParser.TokenType.LITERAL)) {
                if (annotation.extend() && !nested) {
                    throw new BadCommandClassException(src, new BadCommandMethodException(m, "'extend = true' command methods are only valid inside @Command annotated classes"));
                }
                if (!annotation.extend()) {
                    CommandParser.Token[] newTokens = new CommandParser.Token[tokens.length + 1];
                    newTokens[0] = new CommandParser.Token(0, m.getName(), CommandParser.TokenType.LITERAL);
                    System.arraycopy(tokens, 0, newTokens, 1, tokens.length);
                    tokens = newTokens;
                }
            }
            if (nested) {
                CommandParser.Token[] newTokens = new CommandParser.Token[outerTokens.length + tokens.length];
                System.arraycopy(outerTokens, 0, newTokens, 0, outerTokens.length);
                System.arraycopy(tokens, 0, newTokens, outerTokens.length, tokens.length);
                tokens = newTokens;
            }

            LiteralArgumentBuilder<T> commandTree;
            try {
                commandTree = this.commandParser_.brigadierCommand(tokens, m);
            } catch (BadCommandMethodException e) {
                throw new BadCommandClassException(src, e);
            }


            this.dispatcher_.register(commandTree);
        }

        this.instanceClasses_.add(src.getClass());
    }

    public ArgumentTypeRegistry getArgumentTypeRegistry() {
        return this.argumentTypeResolver_;
    }

}