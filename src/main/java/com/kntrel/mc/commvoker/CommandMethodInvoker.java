package com.kntrel.mc.commvoker;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class CommandMethodInvoker<T> implements Command<T> {

    private final Method method_;
    private final String[] arguments_;
    private final Object instance_;


    CommandMethodInvoker(Object instance, Method method, String[] arguments) {
        this.instance_ = instance;
        this.method_ = method;
        this.arguments_ = arguments;
    }


    @Override public int run(CommandContext<T> context) {
        Object[] args = new Object[this.arguments_.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = context.getArgument(this.arguments_[i], Object.class);
        }
        try {
            this.method_.invoke(this.instance_, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }
}
