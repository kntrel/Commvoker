package com.kntrel.mc.commvoker.argument;

import com.kntrel.mc.commvoker.command.CommandDefinition;
import com.kntrel.mc.commvoker.command.CommandPattern;
import com.kntrel.mc.commvoker.command.CommandPatternToken;
import com.kntrel.mc.commvoker.command.CommandToken;
import com.mojang.brigadier.arguments.ArgumentType;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ArgumentContext extends ParameterContext {

    //FIELDS
    private final CommandPattern command_;


    //CONSTRUCTORS
    public ArgumentContext(
            Parameter parameter,
            Type type,
            Method method,
            int parameterIndex,
            CommandPattern command
    ) {
        super(parameter, type, method, parameterIndex);
        this.command_ = Objects.requireNonNull(command, "command");
    }
    public ArgumentContext(ArgumentContext other) {
        super(other);
        this.command_ = other.command_;
    }


    //GETTERS
    public CommandPattern command() { return this.command_; }

    //IMPLEMENTATION
    @Override public boolean equals(Object o) {
        if (!super.equals(o)) { return false; }
        if (!(o instanceof ArgumentContext other)) { return false; }
        return this.command_.equals(other.command_);
    }

    @Override public int hashCode() {
        return super.hashCode() + Objects.hash(this.command_);
    }
}