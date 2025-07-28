package com.kntrel.mc.commvoker.argument.bind;

import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.mc.commvoker.argument.ParameterContext;
import com.kntrel.mc.commvoker.argument.type.ContextualArgumentType;
import com.kntrel.mc.commvoker.argument.type.ImplicitArgumentType;
import com.kntrel.util.Priority;
import com.mojang.brigadier.arguments.ArgumentType;
import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ArgumentBinder<T> {

    public static <T> ArgumentBinder.Undefined<T> argument(Function<ArgumentContext, ArgumentType<T>> fetcher) {
        return new ArgumentBinder.Undefined<>(fetcher, null);
    }
    public static <S, T> Implicit<S, T> implicit(Function<ParameterContext, ImplicitArgumentType<S, T>> fetcher) {
        return new Implicit<>(fetcher);
    }
    public static <S, I, T> ArgumentBinder.Defined<S, T> contextual(Function<ArgumentContext, ContextualArgumentType<S, ?, T>> fetcher) {
        return new Defined<>(null, null, fetcher, null);
    }
    public static <T> ArgumentBinder.Undefined<T> argument(Supplier<ArgumentType<T>> fetcher) {
        return argument(ctx -> fetcher.get());
    }
    public static <S, T> Implicit<S, T> implicit(Supplier<ImplicitArgumentType<S, T>> fetcher) {
        return implicit(ctx -> fetcher.get());
    }
    public static <S, T> ArgumentBinder.Defined<S, T> contextual(Supplier<ContextualArgumentType<S, ?, T>> fetcher) {
        return contextual(ctx -> fetcher.get());
    }
    public static <T> ArgumentBinder.Undefined<T> compose(Function<ArgumentGatherer<?>, ArgumentType<T>> fetcher) {
        return new ArgumentBinder.Undefined<>(null, fetcher);
    }
    public static <S, T> ArgumentBinder.Defined<S, T> composeContextual(Function<ArgumentGatherer<S>, ContextualArgumentType<S, ?, T>> fetcher) {
        return new ArgumentBinder.Defined<>(null, null, null, fetcher);
    }




    private static abstract class Base<C, T, B, I extends Base<C, T, B, I>> {

        private final I instance_;
        protected Class<T> type;
        protected Class<? extends Annotation> annotation;
        protected Predicate<C> condition;
        protected Priority priority;

        @SuppressWarnings("unchecked")
        Base() {
            this.instance_ = (I) this;
        }
        Base(Base<C, T, ?, ?> o) {
            this();
            this.type = o.type;
            this.annotation = o.annotation;
            this.condition = o.condition;
            this.priority = o.priority;
        }

        public I toClass(Class<T> type) {
            this.type = type;
            return this.instance_;
        }
        public I toAnnotation(Class<? extends Annotation> type) {
            this.annotation = type;
            return this.instance_;
        }
        public I toCondition(Predicate<C> condition) {
            this.condition = condition;
            return this.instance_;
        }
        public I withPriority(Priority priority) {
            this.priority = priority;
            return this.instance_;
        }
        public abstract B bind();
    }

    public static class Undefined<T> extends Base<ArgumentContext, T, UndefinedArgumentBinding<T>, Undefined<T>> {

        private final Function<ArgumentContext, ArgumentType<T>> typeSupplier_;
        private final Function<ArgumentGatherer<?>, ArgumentType<T>> composeSupplier_;


        private Undefined(
                Function<ArgumentContext, ArgumentType<T>> typeSupplier,
                Function<ArgumentGatherer<?>, ArgumentType<T>> composeSupplier
        ) {
            this.typeSupplier_ = typeSupplier;
            this.composeSupplier_ = composeSupplier;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public <S> ArgumentBinder.Defined<S, T> requires(Predicate<S> requirement) {
            ArgumentBinder.Defined<S, T> delegate = (this.typeSupplier_ != null)
                ? new Defined<>(this, (Function) this.typeSupplier_, null, null, null)
                : new Defined<>(this, null, (Function) this.composeSupplier_, null, null);

            delegate.requires(requirement);
            return delegate;
        }


        @Override public UndefinedArgumentBinding<T> bind() {
            Priority priority = (this.priority == null) ? Priority.NORMAL : this.priority;

            if (this.typeSupplier_ != null) {
                return new UndefinedArgumentBinding.Type<>(
                        this.typeSupplier_, this.type, this.annotation, this.condition, priority
                );
            }
            return new UndefinedArgumentBinding.Composed<>(
                    this.composeSupplier_, this.type, this.annotation, this.condition, priority
            );
        }
    }

    public static class Defined<S, T> extends Base<ArgumentContext, T, ArgumentBinding<S, T>, Defined<S, T>> {

        private final Function<ArgumentContext, ArgumentType<T>> typeSupplier_;
        private final Function<ArgumentGatherer<S>, ArgumentType<T>> composeSupplier_;
        private final Function<ArgumentContext, ContextualArgumentType<S, ?, T>> contextualSupplier_;
        private final Function<ArgumentGatherer<S>, ContextualArgumentType<S, ?, T>> composeContextualSupplier_;
        private Predicate<S> requirement;


        private Defined(
                Function<ArgumentContext, ArgumentType<T>> typeSupplier,
                Function<ArgumentGatherer<S>, ArgumentType<T>> composeSupplier,
                Function<ArgumentContext, ContextualArgumentType<S, ?, T>> contextualSupplier,
                Function<ArgumentGatherer<S>, ContextualArgumentType<S, ?, T>> composeContextualSupplier
        ) {
            this.typeSupplier_ = typeSupplier;
            this.composeSupplier_ = composeSupplier;
            this.contextualSupplier_ = contextualSupplier;
            this.composeContextualSupplier_ = composeContextualSupplier;
        }
        private Defined(
                Base<ArgumentContext, T, ?, ?> o,
                Function<ArgumentContext, ArgumentType<T>> typeSupplier,
                Function<ArgumentGatherer<S>, ArgumentType<T>> composeSupplier,
                Function<ArgumentContext, ContextualArgumentType<S, ?, T>> contextualSupplier,
                Function<ArgumentGatherer<S>, ContextualArgumentType<S, ?, T>> composeContextualSupplier
        ) {
            super(o);
            this.typeSupplier_ = typeSupplier;
            this.composeSupplier_ = composeSupplier;
            this.contextualSupplier_ = contextualSupplier;
            this.composeContextualSupplier_ = composeContextualSupplier;
        }

        public ArgumentBinder.Defined<S, T> requires(Predicate<S> requirement) {
            this.requirement = requirement;
            return this;
        }

        @Override public ArgumentBinding<S, T> bind() {
            Priority priority = (this.priority == null) ? Priority.NORMAL : this.priority;


            if (this.typeSupplier_ != null) {
                return new ArgumentBinding.Type<>(
                        this.typeSupplier_, this.type, this.annotation, this.condition, this.requirement, priority
                );
            }

            if (this.composeSupplier_ != null) {
                return new ArgumentBinding.Composed<>(
                        this.composeSupplier_, this.type, this.annotation, this.condition, this.requirement, priority
                );
            }

            if (this.contextualSupplier_ != null) {
                return new ArgumentBinding.Contextual<>(
                        (Function) this.contextualSupplier_, this.type, this.annotation, this.condition, this.requirement, priority
                );
            }

            return new ArgumentBinding.ComposedContextual<>(
                    (Function) this.composeContextualSupplier_, this.type, this.annotation, this.condition, this.requirement, priority
            );
        }
    }


    public static class Implicit<S, T> extends Base<ParameterContext, T, ImplicitArgumentBinding<S, T>, Implicit<S, T>> {

        private final Function<ParameterContext, ImplicitArgumentType<S, T>> virtualSupplier_;
        private Predicate<S> requirement;


        private Implicit(
                Function<ParameterContext, ImplicitArgumentType<S, T>> virtualSupplier
        ) {
            this.virtualSupplier_ = virtualSupplier;
        }

        public Implicit<S, T> requires(Predicate<S> requirement) {
            this.requirement = requirement;
            return this;
        }

        @Override public ImplicitArgumentBinding<S, T> bind() {
            Priority priority = (this.priority == null) ? Priority.NORMAL : this.priority;

            return new ArgumentBinding.Implicit<>(
                    this.virtualSupplier_, this.type, this.annotation, this.condition, this.requirement, priority
            );
        }
    }
}
