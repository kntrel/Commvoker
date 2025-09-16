package com.kntrel.mc.commvoker.error;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

public interface CommandExceptionHandler<E extends Throwable> {

    Class<E> exceptionType();
    CommandSyntaxException handle(E exception);
    default boolean handles(E exception) {
        return true;
    }
}
