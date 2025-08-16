package com.kntrel.mc.commvoker.argument.binder;

import com.kntrel.mc.commvoker.argument.binding.ArgumentBinding;
import com.kntrel.mc.commvoker.argument.context.ParameterContext;
import com.kntrel.util.Priority;
import com.mojang.brigadier.context.CommandContext;
import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Predicate;

record ImplicitArgumentBinding<S, T>(
        Function<CommandContext<? extends S>, T> implyer,
        Class<T> toClass,
        Class<? extends Annotation> toAnnotation,
        Predicate<ParameterContext> toCondition,
        Priority priority,
        Predicate<S> requirement
) implements ArgumentBinding.Implicit<S, T> {}
