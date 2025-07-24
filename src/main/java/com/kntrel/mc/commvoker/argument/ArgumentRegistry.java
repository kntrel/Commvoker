package com.kntrel.mc.commvoker.argument;

import com.kntrel.mc.commvoker.argument.bind.ArgumentBinding;
import com.kntrel.mc.commvoker.argument.bind.UndefinedArgumentBinding;
import java.util.Arrays;
import java.util.Collection;

public interface ArgumentRegistry<S> {

    void register(ArgumentBinding<S, ?> binding);

    default void register(ArgumentBinding<S, ?>... bindings) {
        Arrays.stream(bindings).forEach(this::register);
    }

    default void register(Collection<ArgumentBinding<S,?>> bindings) {
        bindings.forEach(this::register);
    }

    default void register(UndefinedArgumentBinding<?> binding) {
        this.register(binding.define());
    }

    default void register(UndefinedArgumentBinding<?>... bindings) {
        Arrays.stream(bindings).forEach(this::register);
    }

}
