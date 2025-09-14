package com.kntrel.mc.commvoker.argument.binder;

import com.kntrel.mc.commvoker.argument.binding.ArgumentBinding;
import com.kntrel.mc.commvoker.argument.binding.Contextualizer;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.argument.context.ParameterContext;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.kntrel.util.Priority;
import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Predicate;

record ImplicitArgumentBinding<S, T>(
        Function<ExecutionContext<? extends S>, T> implyer,
        Class<T> toClass,
        Class<? extends Annotation> toAnnotation,
        Predicate<ParameterContext> toCondition,
        Priority priority,
        Predicate<S> requirement
) implements ArgumentBinding.Implicit<S, T> {
    @Override
    public ArgumentDescriptor<S, T> descriptor() {
        return new ArgumentDescriptor<>() {
            @Override public Contextualizer<S, T> contextualizer() { return implyer::apply; }
            @Override public Predicate<S> requirement() { return ImplicitArgumentBinding.this.requirement(); }
        };
    }
}
