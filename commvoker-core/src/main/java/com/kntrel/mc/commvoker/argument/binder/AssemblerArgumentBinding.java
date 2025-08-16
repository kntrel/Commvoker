package com.kntrel.mc.commvoker.argument.binder;

import com.kntrel.mc.commvoker.argument.context.ArgumentContext;
import com.kntrel.mc.commvoker.argument.context.ArgumentGatherer;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.CompiledAssembler;
import com.kntrel.mc.commvoker.argument.binding.ArgumentBinding;
import com.kntrel.mc.commvoker.argument.binding.ArgumentDescriptor;
import com.kntrel.util.Priority;
import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Predicate;

record AssemblerArgumentBinding<S, T>(
        Function<ArgumentGatherer<? extends S>, Assembler<S, T>> supplier,
        Class<T> toClass,
        Class<? extends Annotation> toAnnotation,
        Predicate<ArgumentContext> toCondition,
        Priority priority,
        Predicate<S> requirement
) implements ArgumentBinding.Descriptive<S, T> {

    @Override
    public ArgumentDescriptor<S, T> descriptor(ArgumentGatherer<? extends S> ctx) {
        return CompiledAssembler.of(supplier().apply(ctx));
    }
}