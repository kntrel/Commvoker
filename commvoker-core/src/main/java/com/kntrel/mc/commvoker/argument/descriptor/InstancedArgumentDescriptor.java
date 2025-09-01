package com.kntrel.mc.commvoker.argument.descriptor;

import com.kntrel.mc.commvoker.argument.binding.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.argument.binding.Contextualizer;
import java.lang.reflect.Type;

public interface InstancedArgumentDescriptor<S, T> extends TypedArgumentDescriptor<S, T> {

    static <S, T> InstancedArgumentDescriptor<S, T> of(TypedArgumentDescriptor<S, T> delegate, T value) {
        return new InstancedArgumentDescriptor<>() {
            @Override public Type type() { return delegate.type(); }
            @Override public CommandTemplate<S> template() { return delegate.template(); }
            @Override public Contextualizer<S, T> contextualizer() { return delegate.contextualizer(); }
            @Override public T value() { return value; }
        };
    }
    static <S, T> InstancedArgumentDescriptor<S, T> of(ArgumentDescriptor<S, T> delegate, T value, Type type) {
        return of(TypedArgumentDescriptor.of(delegate, type), value);
    }
    static <S, T> InstancedArgumentDescriptor<S, T> of(ArgumentDescriptor<S, T> delegate, T value) {
        return of(delegate, value, value.getClass());
    }

    T value();
}
