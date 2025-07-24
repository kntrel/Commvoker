package com.kntrel.mc.commvoker.argument.bind;

import com.kntrel.mc.commvoker.argument.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.ArgumentResolutionContext;
import com.kntrel.mc.commvoker.argument.ParameterContext;
import com.kntrel.mc.commvoker.argument.type.VirtualArgumentType;
import com.kntrel.util.Either;
import com.kntrel.util.Multipredicate;
import com.kntrel.util.Priority;
import com.mojang.brigadier.arguments.ArgumentType;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public sealed interface ArgumentBinding<S, T> extends SimpleArgumentBinding<T> {

    ArgumentDescriptor<S, T> descriptor(ArgumentResolutionContext<S> ctx);

    record Type<S, T>(
            Function<ArgumentResolutionContext<S>, ArgumentType<T>> supplier,
            Class<T> toClass,
            Class<? extends Annotation> toAnnotation,
            Predicate<ParameterContext> toCondition,
            Predicate<S> requirement,
            Priority priority
    ) implements ArgumentBinding<S, T>, Function<ArgumentResolutionContext<S>, ArgumentType<T>>  {
        @Override
        public ArgumentType<T> apply(ArgumentResolutionContext<S> ctx) {
            return this.supplier.apply(ctx);
        }

        @Override
        public ArgumentDescriptor<S, T> descriptor(ArgumentResolutionContext<S> ctx) {
            return new ArgumentDescriptor<>(Either.ofTheOne(this.apply(ctx)), this.requirement);
        }
    }

    record Virtual<S, T>(
            Function<ArgumentResolutionContext<S>, VirtualArgumentType<S, T>> supplier,
            Class<T> toClass,
            Class<? extends Annotation> toAnnotation,
            Predicate<ParameterContext> toCondition,
            Predicate<S> requirement,
            Priority priority
     ) implements ArgumentBinding<S, T>, Function<ArgumentResolutionContext<S>, VirtualArgumentType<S, T>> {

        @Override public VirtualArgumentType<S, T> apply(ArgumentResolutionContext<S> ctx) {
            return this.supplier.apply(ctx);
        }
        @Override
        public ArgumentDescriptor<S, T> descriptor(ArgumentResolutionContext<S> ctx) {
            return new ArgumentDescriptor<>(Either.ofTheOther(this.apply(ctx)), this.requirement);
        }
    }

    record Composed<S, T>(
            Function<ArgumentGatherer<S>, ArgumentType<T>> supplier,
            Class<T> toClass,
            Class<? extends Annotation> toAnnotation,
            Predicate<ParameterContext> toCondition,
            Predicate<S> requirement,
            Priority priority

    ) implements ArgumentBinding<S, T>, Function<ArgumentResolutionContext<S>, ArgumentType<T>> {

        @Override public ArgumentType<T> apply(ArgumentResolutionContext<S> ctx) {
            return this.supplier.apply(new ArgumentGatherer<>(ctx));
        }
        @Override
        public ArgumentDescriptor<S, T> descriptor(ArgumentResolutionContext<S> ctx) {
            ArgumentGatherer<S> gatherer = new ArgumentGatherer<>(ctx);
            ArgumentType<T> type = this.supplier.apply(gatherer);

            Set<Predicate<S>> requirements = new LinkedHashSet<>();
            if (this.requirement != null) {
                requirements.add(this.requirement);
            }
            requirements.addAll(gatherer.getRequirements());

            return new ArgumentDescriptor<>(Either.ofTheOne(type), Multipredicate.and(requirements));
        }
    }
}
