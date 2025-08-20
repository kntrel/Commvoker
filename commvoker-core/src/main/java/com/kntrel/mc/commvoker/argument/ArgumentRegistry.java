package com.kntrel.mc.commvoker.argument;

import com.kntrel.mc.commvoker.argument.binding.ArgumentBinding;
import com.kntrel.mc.commvoker.argument.context.ParameterContext;

import java.util.Arrays;
import java.util.Collection;

public interface ArgumentRegistry<S> {

    void register(ArgumentBinding<? super S, ? extends ParameterContext, ?> binding);


    default void register(ArgumentBinding<? super S, ? extends ParameterContext, ?>... bindings) {
        Arrays.stream(bindings).forEach(this::register);
    }

    default void register(Collection<ArgumentBinding<? super S, ? extends ParameterContext, ?>> bindings) {
        bindings.forEach(this::register);
    }
}
