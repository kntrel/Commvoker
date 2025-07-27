package com.kntrel.mc.commvoker.argument.bind;

import com.kntrel.mc.commvoker.argument.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.mc.commvoker.argument.ParameterContext;
import com.kntrel.mc.commvoker.argument.type.VirtualArgumentType;
import com.kntrel.util.Multipredicate;
import com.kntrel.util.Priority;
import com.mojang.brigadier.arguments.ArgumentType;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ArgumentBinding<S, T> extends SimpleArgumentBinding<ArgumentContext, T> {

    ArgumentDescriptor.Parsed<S, T> descriptor(ArgumentGatherer<S> ctx);

    record Type<S, T>(
            Function<ArgumentContext, ArgumentType<T>> supplier,
            Class<T> toClass,
            Class<? extends Annotation> toAnnotation,
            Predicate<ArgumentContext> toCondition,
            Predicate<S> requirement,
            Priority priority
    ) implements ArgumentBinding<S, T>, Function<ArgumentContext, ArgumentType<T>>  {
        @Override
        public ArgumentType<T> apply(ArgumentContext ctx) {
            return this.supplier.apply(ctx);
        }

        @Override
        public ArgumentDescriptor.Parsed<S, T> descriptor(ArgumentGatherer<S> ctx) {
            return ArgumentDescriptor.of(this.apply(ctx), this.requirement);
        }
    }

    record Virtual<S, T>(
            Function<ParameterContext, VirtualArgumentType<S, T>> supplier,
            Class<T> toClass,
            Class<? extends Annotation> toAnnotation,
            Predicate<ParameterContext> toCondition,
            Predicate<S> requirement,
            Priority priority
     ) implements VirtualArgumentBinding<S, T>, Function<ParameterContext, VirtualArgumentType<S, T>> {

        @Override public VirtualArgumentType<S, T> apply(ParameterContext ctx) {
            return this.supplier.apply(ctx);
        }
        @Override
        public ArgumentDescriptor.Virtual<S, T> descriptor(ParameterContext ctx) {
            return ArgumentDescriptor.of(this.apply(ctx), this.requirement);
        }
    }

    record Composed<S, T>(
            Function<ArgumentGatherer<S>, ArgumentType<T>> supplier,
            Class<T> toClass,
            Class<? extends Annotation> toAnnotation,
            Predicate<ArgumentContext> toCondition,
            Predicate<S> requirement,
            Priority priority

    ) implements ArgumentBinding<S, T>, Function<ArgumentGatherer<S>, ArgumentType<T>> {

        @Override public ArgumentType<T> apply(ArgumentGatherer<S> ctx) {
            return this.supplier.apply(ctx);
        }
        @Override
        public ArgumentDescriptor.Parsed<S, T> descriptor(ArgumentGatherer<S> ctx) {
            ArgumentType<T> type = this.supplier.apply(ctx);

            Set<Predicate<S>> requirements = new LinkedHashSet<>();
            if (this.requirement != null) {
                requirements.add(this.requirement);
            }
            requirements.addAll(ctx.getRequirements());
            
            return ArgumentDescriptor.of(type, Multipredicate.and(requirements));
        }
    }
}
