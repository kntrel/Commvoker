package com.kntrel.mc.commvoker.argument;

import com.kntrel.mc.commvoker.argument.binding.ArgumentBinding;

import java.util.Arrays;
import java.util.Collection;

public interface ArgumentRegistry<S> {

    void register(ArgumentBinding<? super S, ?> binding);


    default void register(ArgumentBinding<? super S, ?>... bindings) {
        Arrays.stream(bindings).forEach(this::register);
    }

    default void register(Collection<ArgumentBinding<? super S,?>> bindings) {
        bindings.forEach(this::register);
    }
}
