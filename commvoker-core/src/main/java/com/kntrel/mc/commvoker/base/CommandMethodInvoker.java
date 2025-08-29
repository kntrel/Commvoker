package com.kntrel.mc.commvoker.base;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CommandMethodInvoker<S> implements Command<S> {

    private final Method method_;
    private final ArgumentParser<S>[] argumentParsers_;
    private final Object instance_;


    CommandMethodInvoker(Object instance, Method method, ArgumentParser<S>[] arguments) {
        if (method.getParameterCount() != arguments.length) {
            throw new IllegalArgumentException("Method argument count isn't equal to ArgumentParser array length");
        }

        this.instance_ = instance;
        this.method_ = method;
        this.argumentParsers_ = arguments;
    }


    @Override public int run(CommandContext<S> ctx) {
        List<Object> args = new ArrayList<>(this.argumentParsers_.length);
        Map<String, Object> bag = new HashMap<>();
        for (ArgumentParser<S> parser : this.argumentParsers_) {
            Object o = parser.parse(ctx, args, bag);
            args.add(o);
        }

        Object returned;
        try {
            returned = this.method_.invoke(this.instance_, args.toArray());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        if (Number.class.isAssignableFrom(this.method_.getReturnType())) {
            return (int) returned;
        }

        return 0;
    }
}
