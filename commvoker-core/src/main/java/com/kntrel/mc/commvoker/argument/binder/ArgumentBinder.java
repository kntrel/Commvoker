package com.kntrel.mc.commvoker.argument.binder;

import com.kntrel.mc.commvoker.argument.context.ArgumentContext;
import com.kntrel.mc.commvoker.argument.context.ArgumentGatherer;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.argument.binding.ArgumentBinding;
import com.kntrel.util.Priority;
import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ArgumentBinder<S, T> {

    //FACTORY
    public static <S, T> ArgumentBinder<S, T> argumentAssembler(Function<ArgumentGatherer<? extends S>, Assembler<S, T>> supplier) {
        return new ArgumentBinder<>(supplier);
    }
    public static <S, T> ArgumentBinder<S, T> argumentAssembler(Supplier<Assembler<S, T>> supplier) {
        return new ArgumentBinder<>((ctx) -> supplier.get());
    }


    //FIELDS
    private final Function<ArgumentGatherer<? extends S>, Assembler<S, T>> supplier_;
    private Class<T> type_;
    private Class<? extends Annotation> annotation_;
    private Predicate<ArgumentContext> condition_;
    private Priority priority_;
    private Predicate<S> requirement_;


    //CONSTRUCTOR
    private ArgumentBinder(Function<ArgumentGatherer<? extends S>, Assembler<S, T>> supplier) {
        this.supplier_ = supplier;
        this.type_ = null;
        this.annotation_ = null;
        this.condition_ = null;
        this.priority_ = null;
        this.requirement_ = null;

    }


    //SETTERS
    public ArgumentBinder<S, T> toClass(Class<T> type) {
        this.type_ = type;
        return this;
    }
    public ArgumentBinder<S, T>  toAnnotation(Class<? extends Annotation> type) {
        this.annotation_ = type;
        return this;
    }
    public ArgumentBinder<S, T>  toCondition(Predicate<ArgumentContext> condition) {
        this.condition_ = condition;
        return this;
    }
    public ArgumentBinder<S, T>  withPriority(Priority priority) {
        this.priority_ = priority;
        return this;
    }
    public ArgumentBinder<S, T>  requires(Predicate<S> requirement) {
        this.requirement_ = requirement;
        return this;
    }


    //BUILD
    public ArgumentBinding<S, ?, T> bind() {
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
