package com.kntrel.mc.commvoker.argument.binder;

import com.kntrel.mc.commvoker.argument.context.ArgumentContext;
import com.kntrel.mc.commvoker.argument.context.ArgumentGatherer;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.argument.context.ParameterContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.argument.binding.ArgumentBinding;
import com.kntrel.util.Priority;
import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ArgumentBinder<S, T> {

    private ArgumentBinder() {}


    //FACTORY
    public static <S, T> Descriptive<S, T> argumentAssembler(Function<ArgumentGatherer<? extends S>, Assembler<S, T>> supplier) {
        return new Descriptive<>(supplier);
    }
    public static <S, T> Descriptive<S, T> argumentAssembler(Supplier<Assembler<S, T>> supplier) {
        return argumentAssembler((ctx) -> supplier.get());
    }
    public static <S, T> Implicit<S, T> implicit(Function<ExecutionContext<? extends S>, T> implyer) {
        return new Implicit<>(implyer);
    }


    private static abstract class Base<S, T, C extends ParameterContext, I extends Base<S, T, C, I>> {
        //FIELDS
        private final I instance_;
        protected Class<T> type_;
        protected Class<? extends Annotation> annotation_;
        protected Predicate<C> condition_;
        protected Priority priority_;
        protected Predicate<S> requirement_;


        //CONSTRUCTOR
        private Base() {
            this.instance_ = (I) this;
            this.type_ = null;
            this.annotation_ = null;
            this.condition_ = null;
            this.priority_ = null;
            this.requirement_ = null;

        }


        //SETTERS
        public I toClass(Class<T> type) {
            this.type_ = type;
            return this.instance_;
        }
        public I toAnnotation(Class<? extends Annotation> type) {
            this.annotation_ = type;
            return this.instance_;
        }
        public I toCondition(Predicate<C> condition) {
            this.condition_ = condition;
            return this.instance_;
        }
        public I withPriority(Priority priority) {
            this.priority_ = priority;
            return this.instance_;
        }
        public I requires(Predicate<S> requirement) {
            this.requirement_ = requirement;
            return this.instance_;
        }


        //BUILD
        public abstract ArgumentBinding<S, C, T> bind();
    }


    public static class Descriptive<S, T> extends Base<S, T, ArgumentContext, Descriptive<S, T>> {

        //FIELDS
        private final Function<ArgumentGatherer<? extends S>, Assembler<S, T>> supplier_;


        //CONSTRUCTOR
        private Descriptive(Function<ArgumentGatherer<? extends S>, Assembler<S, T>> supplier) {
            this.supplier_ = supplier;
        }


        //IMPLEMENTATION
        @Override public ArgumentBinding<S, ArgumentContext, T> bind() {
            Priority priority = (this.priority_ != null) ? this.priority_ : Priority.NORMAL;
            return new AssemblerArgumentBinding<>(
                    this.supplier_,
                    this.type_,
                    this.annotation_,
                    this.condition_,
                    priority,
                    this.requirement_
            );
        }
    }


    public static class Implicit<S, T> extends Base<S, T, ParameterContext, Implicit<S, T>> {

        //FIELDS
        private final Function<ExecutionContext<? extends S>, T> implyer_;


        //CONSTRUCTOR
        private Implicit(Function<ExecutionContext<? extends S>, T> implyer) {
            this.implyer_ = implyer;
        }


        //IMPLEMENTATION
        @Override public ArgumentBinding<S, ParameterContext, T> bind() {
            Priority priority = (this.priority_ != null) ? this.priority_ : Priority.NORMAL;
            return new ImplicitArgumentBinding<>(
                    this.implyer_,
                    this.type_,
                    this.annotation_,
                    this.condition_,
                    priority,
                    this.requirement_
            );
        }
    }
}
