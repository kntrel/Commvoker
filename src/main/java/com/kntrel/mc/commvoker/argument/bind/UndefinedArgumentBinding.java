package com.kntrel.mc.commvoker.argument.bind;

import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.util.Priority;
import com.mojang.brigadier.arguments.ArgumentType;
import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Predicate;

public interface UndefinedArgumentBinding<T> extends SimpleArgumentBinding<ArgumentContext, T> {

    default <S> ArgumentBinding<S, T> define() {
        return this.defineWithRequirement(null);
    }
    <S> ArgumentBinding<S, T> defineWithRequirement(Predicate<S> requirement);

    record Type<T>(
        Function<ArgumentContext, ArgumentType<T>> supplier,
        Class<T> toClass,
        Class<? extends Annotation> toAnnotation,
        Predicate<ArgumentContext> toCondition,
        Priority priority
    ) implements UndefinedArgumentBinding<T> {

        @Override public <S> ArgumentBinding.Type<S, T> defineWithRequirement(Predicate<S> requirement) {
            return new ArgumentBinding.Type<>(
                    this.supplier, this.toClass, this.toAnnotation, this.toCondition, requirement, this.priority
            );
        }

    }

    record Composed<T>(
        Function<ArgumentGatherer<?>, ArgumentType<T>> supplier,
        Class<T> toClass,
        Class<? extends Annotation> toAnnotation,
        Predicate<ArgumentContext> toCondition,
        Priority priority
    ) implements UndefinedArgumentBinding<T> {

        @Override public <S> ArgumentBinding.Composed<S, T> defineWithRequirement(Predicate<S> requirement) {
            return new ArgumentBinding.Composed<>(this.supplier::apply, this.toClass, this.toAnnotation, this.toCondition, requirement, this.priority);
        }
    }
}
