package com.kntrel.mc.commvoker.callback;

import com.kntrel.mc.commvoker.command.CommandMethod;
import com.kntrel.mc.commvoker.command.CommandMethodContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public interface ReturnCallback<S, T> {

    default boolean listensTo(CommandMethod commandMethod) { return true; }
    void onReturn(CommandMethodContext<? extends S> context, T returnValue) throws CommandSyntaxException;

}