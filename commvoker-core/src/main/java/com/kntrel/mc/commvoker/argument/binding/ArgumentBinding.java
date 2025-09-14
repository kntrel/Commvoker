package com.kntrel.mc.commvoker.argument.binding;

import com.kntrel.mc.commvoker.argument.context.ArgumentContext;
import com.kntrel.mc.commvoker.argument.context.ArgumentGatherer;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.argument.context.ParameterContext;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.descriptor.TemplatedArgumentDescriptor;
import com.kntrel.util.Priority;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public sealed interface ArgumentBinding<S, C extends ParameterContext, T> extends Predicate<C>, Comparable<ArgumentBinding<S, C, T>> {

    non-sealed interface Implicit<S, T> extends ArgumentBinding<S, ParameterContext, T> {

        ArgumentDescriptor<S, T> descriptor();
    }

    non-sealed interface Descriptive<S, T> extends ArgumentBinding<S, ArgumentContext, T> {

        TemplatedArgumentDescriptor<S, T> descriptor(ArgumentGatherer<? extends S> ctx);
    }

    Class<T> toClass();
    Class<? extends Annotation> toAnnotation();
    Predicate<C> toCondition();
    Priority priority();

    default @Override boolean test(C ctx) {
        Class<?> clazz = this.toClass();
        if (clazz != null && ctx.type() instanceof Class<?> c && !c.isAssignableFrom(clazz)) {
            return false;
        }

        Class<? extends Annotation> annotation = this.toAnnotation();
        if (annotation != null && !ctx.isAnnotationPresent(annotation)) {
            return false;
        }

        Predicate<C> condition = this.toCondition();
        if (condition != null) {
            return condition.test(ctx);
        }

        return true;
    }

    default @Override int compareTo(ArgumentBinding<S, C, T> o) {
        return this.priority().compareTo(o.priority());
    }
}
