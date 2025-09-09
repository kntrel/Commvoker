package com.kntrel.mc.commvoker.argument.descriptor;

import com.kntrel.mc.commvoker.argument.binding.Contextualizer;
import java.util.function.Predicate;

public interface InstancedArgumentDescriptor<S, T> extends ArgumentDescriptor<S, T> {

    static <S, T> InstancedArgumentDescriptor<S, T> of(ArgumentDescriptor<S, T> delegate, T value) {
        return new InstancedArgumentDescriptor<>() {
            @Override public Contextualizer<S, T> contextualizer() { return delegate.contextualizer(); }
            @Override public Predicate<S> requirement() { return delegate.requirement(); }
            @Override public T value() { return value; }
        };
    }

    T value();
}
