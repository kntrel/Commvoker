package com.kntrel.mc.commvoker.argument.descriptor;

import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.argument.binding.Contextualizer;
import java.lang.reflect.Type;
import java.util.function.Predicate;

public interface CompiledArgumentDescriptor<S, T> extends TypedArgumentDescriptor<S, T> {

    static <S, T> CompiledArgumentDescriptor<S, T> of(TypedArgumentDescriptor<S, T> descriptor, CommandTreeGate<S> gate) {

        return new CompiledArgumentDescriptor<S, T>() {
            @Override public CommandTemplate<S> template() { return descriptor.template(); }
            @Override public CommandTreeGate<S> compiled() { return gate; }
            @Override public Type type() { return descriptor.type(); }
            @Override public Contextualizer<S, T> contextualizer() { return descriptor.contextualizer(); }
            @Override public Predicate<S> requirement() { return descriptor.requirement(); }
        };
    }
    static <S, T> CompiledArgumentDescriptor<S, T> of(TemplatedArgumentDescriptor<S, T> descriptor, Type type, CommandTreeGate<S> nodes) {
        return of(TypedArgumentDescriptor.of(descriptor, type), nodes);
    }

    CommandTreeGate<S> compiled();
}
