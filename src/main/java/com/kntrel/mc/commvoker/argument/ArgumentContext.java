package com.kntrel.mc.commvoker.argument;

import com.kntrel.mc.commvoker.command.CommandDefinition;
import com.kntrel.mc.commvoker.command.CommandToken;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Objects;

public class ArgumentContext {

    //FIELDS
    private final Parameter parameter_;
    private final Type type_;
    private final Method method_;
    private final int parameterIndex_;
    private final CommandDefinition command_;
    private final int commandTokenIndex_;


    //CONSTRUCTORS
    public ArgumentContext(
            Parameter parameter,
            Type type,
            Method method,
            int parameterIndex,
            CommandDefinition command,
            int commandTokenIndex
    ) {
        if (parameterIndex < 0) {
            throw new IndexOutOfBoundsException(parameterIndex);
        }
        if (commandTokenIndex < 0) {
            throw new IndexOutOfBoundsException(commandTokenIndex);
        }
        if (parameterIndex >= method.getParameterCount()) {
            throw new IndexOutOfBoundsException(parameterIndex);
        }
        if (commandTokenIndex >= command.size()) {
            throw new IndexOutOfBoundsException(commandTokenIndex);
        }

        this.parameter_ = Objects.requireNonNull(parameter, "parameter");
        this.type_ = Objects.requireNonNull(type);
        this.method_ = Objects.requireNonNull(method, "method");
        this.parameterIndex_ = parameterIndex;
        this.command_ = Objects.requireNonNull(command, "command");
        this.commandTokenIndex_ = commandTokenIndex;
    }
    public ArgumentContext(ArgumentContext other) {
        this(other.parameter_, other.type_, other.method_, other.parameterIndex_, other.command_, other.commandTokenIndex_);
    }


    //GETTERS
    public Parameter parameter() { return this.parameter_; }
    public Type type() { return this.type_; }
    public Method method() { return this.method_; }
    public int parameterIndex() { return this.parameterIndex_; }
    public CommandDefinition command() { return this.command_; }
    public int commandTokenIndex() { return this.commandTokenIndex_; }


    //UTILITY
    public <A extends Annotation> A getAnnotation(Class<A> annotation) {
        return this.parameter_.getAnnotation(annotation);
    }
    public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
        return this.parameter_.isAnnotationPresent(annotation);
    }
    public CommandToken commandToken() {
        return this.command_.getTokenAt(this.commandTokenIndex_);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null) { return false; }
        if (this == o) { return true; }
        if (!(o instanceof ArgumentContext other)) { return false; }
        return     parameterIndex_ == other.parameterIndex_
                && commandTokenIndex_ == other.commandTokenIndex_
                && parameter_.equals(other.parameter_)
                && method_.equals(other.method_)
                && command_.equals(other.command_);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameter_, method_, parameterIndex_, command_, commandTokenIndex_);
    }
}