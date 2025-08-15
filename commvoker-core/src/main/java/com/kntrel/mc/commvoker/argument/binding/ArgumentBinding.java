package com.kntrel.mc.commvoker.argument.binding;

import com.kntrel.mc.commvoker.argument.context.ArgumentContext;
import com.kntrel.mc.commvoker.argument.binder.ArgumentGatherer;
import com.kntrel.util.Priority;
import com.mojang.brigadier.context.CommandContext;
import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Predicate;

public sealed interface ArgumentBinding<S, T> extends Predicate<ArgumentContext>, Comparable<ArgumentBinding<S, T>> {

    non-sealed interface Implicit<S, T> extends ArgumentBinding<S, T> {

        Function<CommandContext<? extends S>, T> implyer();
    }

    non-sealed interface Descriptive<S, T> extends ArgumentBinding<S, T> {

        ArgumentDescriptor<S, T> descriptor(ArgumentGatherer<? extends S> ctx);
    }

    Class<T> toClass();
    Class<? extends Annotation> toAnnotation();
    Predicate<ArgumentContext> toCondition();
    Priority priority();

    default @Override boolean test(ArgumentContext ctx) {
        Class<?> clazz = this.toClass();
        if (clazz != null && ctx.type() instanceof Class<?> c && !c.isAssignableFrom(clazz)) {
            return false;
        }

        Class<? extends Annotation> annotation = this.toAnnotation();
        if (annotation != null && !ctx.isAnnotationPresent(annotation)) {
            return false;
        }

        Predicate<ArgumentContext> condition = this.toCondition();
        if (condition != null) {
            return condition.test(ctx);
        }

        return true;
    }

    default @Override int compareTo(ArgumentBinding<S, T> o) {
        return this.priority().compareTo(o.priority());
    }
}
