package com.kntrel.mc.commvoker.argument.bind;

import com.kntrel.mc.commvoker.argument.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.mc.commvoker.argument.ParameterContext;
import com.kntrel.mc.commvoker.argument.type.ContextualArgumentType;
import com.kntrel.mc.commvoker.argument.type.ImplicitArgumentType;
import com.kntrel.util.Multipredicate;
import com.kntrel.util.Priority;
import com.mojang.brigadier.arguments.ArgumentType;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ArgumentBinding<S, T> extends SimpleArgumentBinding<ArgumentContext, T> {

    ArgumentDescriptor<S> descriptor(ArgumentGatherer<S> ctx);

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

    record Implicit<S, T>(
            Function<ParameterContext, ImplicitArgumentType<S, T>> supplier,
            Class<T> toClass,
            Class<? extends Annotation> toAnnotation,
            Predicate<ParameterContext> toCondition,
            Predicate<S> requirement,
            Priority priority
     ) implements ImplicitArgumentBinding<S, T>, Function<ParameterContext, ImplicitArgumentType<S, T>> {

        @Override public ImplicitArgumentType<S, T> apply(ParameterContext ctx) {
            return this.supplier.apply(ctx);
        }
        @Override
        public ArgumentDescriptor.Implicit<S, T> descriptor(ParameterContext ctx) {
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


    record Contextual<S, I, T>(
            Function<ArgumentContext, ContextualArgumentType<S, I, T>> supplier,
            Class<T> toClass,
            Class<? extends Annotation> toAnnotation,
            Predicate<ArgumentContext> toCondition,
            Predicate<S> requirement,
            Priority priority

    ) implements ArgumentBinding<S, T>, Function<ArgumentGatherer<S>, ContextualArgumentType<S, I, T>> {

        @Override public ContextualArgumentType<S, I, T> apply(ArgumentGatherer<S> ctx) {
            return this.supplier.apply(ctx);
        }
        @Override
        public ArgumentDescriptor.Contextual<S, I, T> descriptor(ArgumentGatherer<S> ctx) {

            return ArgumentDescriptor.ofContextual(this.apply(ctx), this.requirement());
        }
    }

    record ComposedContextual<S, I, T>(
            Function<ArgumentGatherer<S>, ContextualArgumentType<S, I, T>> supplier,
            Class<T> toClass,
            Class<? extends Annotation> toAnnotation,
            Predicate<ArgumentContext> toCondition,
            Predicate<S> requirement,
            Priority priority

    ) implements ArgumentBinding<S, T>, Function<ArgumentGatherer<S>, ContextualArgumentType<S, I, T>> {

        @Override public ContextualArgumentType<S, I, T> apply(ArgumentGatherer<S> ctx) {
            return this.supplier.apply(ctx);
        }
        @Override
        public ArgumentDescriptor.Contextual<S, I, T> descriptor(ArgumentGatherer<S> ctx) {
            ContextualArgumentType<S, I, T> type = this.supplier.apply(ctx);

            Set<Predicate<S>> requirements = new LinkedHashSet<>();
            if (this.requirement != null) {
                requirements.add(this.requirement);
            }
            requirements.addAll(ctx.getRequirements());

            return ArgumentDescriptor.ofContextual(type, Multipredicate.and(requirements));
        }
    }
}
