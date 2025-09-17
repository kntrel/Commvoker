package com.kntrel.mc.commvoker.error;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.function.Function;

public interface CommandExceptionResolver {

    void registerHandler(CommandExceptionHandler<? extends Throwable> handler);
    void unregisterHandler(Class<? extends CommandExceptionHandler<? extends Throwable>> handlerClass);
    void unregisterHandler(CommandExceptionHandler<? extends Throwable> handler);
    CommandSyntaxException resolve(Throwable exception);

    default <E extends Throwable> CommandExceptionHandler<E> registerHandler(Class<E> exceptionType, Function<E, CommandSyntaxException> handler) {
        CommandExceptionHandler<E> exceptionHandler = new CommandExceptionHandler<>() {
            @Override public Class<E> exceptionType() { return exceptionType; }
            @Override public CommandSyntaxException handle(E exception) { return handler.apply(exception); }
        };
        this.registerHandler(exceptionHandler);
        return exceptionHandler;
    }
}
