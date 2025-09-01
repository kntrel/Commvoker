package com.kntrel.mc.commvoker.argument.descriptor;

import com.kntrel.mc.commvoker.argument.binding.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.argument.binding.Contextualizer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface TypedArgumentDescriptor<S, T> extends ArgumentDescriptor<S, T> {

    static <S, T> TypedArgumentDescriptor<S, T> of(ArgumentDescriptor<S, T> delegate, Type type) {
        return new TypedArgumentDescriptor<>() {
            @Override public CommandTemplate<S> template() { return delegate.template(); }
            @Override public Contextualizer<S, T> contextualizer() { return delegate.contextualizer(); }
            @Override public Type type() { return type; }
        };
    }
    static <S, T> TypedArgumentDescriptor<S, T> of(ArgumentDescriptor<S, T> delegate, Class<T> cls) {
        return of(delegate, (Type) cls);
    }


    Type type();

    @SuppressWarnings("unchecked")
    default Class<T> classType() {
        Type type = type();
        if (type instanceof Class<?> cls) {
            return (Class<T>) cls;
        } else if (type instanceof ParameterizedType pType) {
            Type rawType = pType.getRawType();
            if (rawType instanceof Class<?> rawClass) {
                return (Class<T>) rawClass;
            }
        }
        throw new IllegalStateException("Type is not a class: " + type);
    }

    default boolean isParameterized() {
        return type() instanceof ParameterizedType;
    }
}