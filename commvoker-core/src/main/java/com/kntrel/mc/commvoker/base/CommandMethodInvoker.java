package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.descriptor.InstancedArgumentDescriptor;
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
        List<InstancedArgumentDescriptor<S, ?>> descriptors = new ArrayList<>(this.argumentParsers_.length);
        Map<String, Object> bag = new HashMap<>();
        for (ArgumentParser<S> parser : this.argumentParsers_) {
            InstancedArgumentDescriptor<S, ?> desc = parser.parse(ctx, descriptors, bag);
            descriptors.add(desc);
        }

        Object[] args = descriptors.stream().map(InstancedArgumentDescriptor::value).toArray();
        Object returned;
        try {
            returned = this.method_.invoke(this.instance_, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        if (Number.class.isAssignableFrom(this.method_.getReturnType())) {
            return (int) returned;
        }

        return 0;
    }
}
