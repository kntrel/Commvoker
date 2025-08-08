package com.kntrel.mc.commvoker.argument;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Objects;

public class ParameterContext {

    //FIELDS
    private final Parameter parameter_;
    private final Type type_;
    private final Method method_;
    private final int parameterIndex_;


    //CONSTRUCTORS
    public ParameterContext(
            Parameter parameter,
            Type type,
            Method method,
            int parameterIndex
    ) {
        if (parameterIndex < 0) {
            throw new IndexOutOfBoundsException(parameterIndex);
        }
        if (parameterIndex >= method.getParameterCount()) {
            throw new IndexOutOfBoundsException(parameterIndex);
        }

        this.parameter_ = Objects.requireNonNull(parameter, "parameter");
        this.type_ = Objects.requireNonNull(type);
        this.method_ = Objects.requireNonNull(method, "method");
        this.parameterIndex_ = parameterIndex;
    }
    public ParameterContext(ParameterContext other) {
        this(other.parameter_, other.type_, other.method_, other.parameterIndex_);
    }


    //GETTERS
    public Parameter parameter() { return this.parameter_; }
    public Type type() { return this.type_; }
    public Method method() { return this.method_; }
    public int parameterIndex() { return this.parameterIndex_; }


    //UTILITY
    public <A extends Annotation> A getAnnotation(Class<A> annotation) {
        return this.parameter_.getAnnotation(annotation);
    }
    public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
        return this.parameter_.isAnnotationPresent(annotation);
    }


    //IMPLEMENTATION
    @Override public boolean equals(Object o) {
        if (o == null) { return false; }
        if (this == o) { return true; }
        if (!(o instanceof ParameterContext other)) { return false; }
        return     this.parameterIndex_ == other.parameterIndex_
                && this.type_.equals(other.type_)
                && this.parameter_.equals(other.parameter_)
                && this.method_.equals(other.method_);
    }
    @Override public int hashCode() {
        return Objects.hash(this.parameter_, this.method_, this.parameterIndex_);
    }
}
