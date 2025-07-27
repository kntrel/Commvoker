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
    private final CommandDefinition command_;
    private final int commandTokenIndex_;
    private final ArgumentType<?>[] previous_;


    //CONSTRUCTORS
    public ArgumentContext(
            Parameter parameter,
            Type type,
            Method method,
            int parameterIndex,
            CommandDefinition command,
            int commandTokenIndex,
            ArgumentType<?>[] previous_
    ) {
        super(parameter, type, method, parameterIndex);

        if (commandTokenIndex < 0) {
            throw new IndexOutOfBoundsException(commandTokenIndex);
        }
        if (commandTokenIndex >= command.size()) {
            throw new IndexOutOfBoundsException(commandTokenIndex);
        }

        this.command_ = Objects.requireNonNull(command, "command");
        this.commandTokenIndex_ = commandTokenIndex;
        this.previous_ = previous_;
    }
    public ArgumentContext(ArgumentContext other) {
        super(other);
        this.command_ = other.command_;
        this.commandTokenIndex_ = other.commandTokenIndex_;
        this.previous_ = other.previous_;
    }


    //GETTERS
    public CommandDefinition command() { return this.command_; }
    public int commandTokenIndex() { return this.commandTokenIndex_; }
    public CommandToken commandToken() {
        return this.command_.getTokenAt(this.commandTokenIndex_);
    }
    public ArgumentType<?>[] previousTypes() {
        return Arrays.copyOf(this.previous_, this.previous_.length);
    }


    //UTILITY
    public int previousCount() {
        return this.previous_.length;

    }
    public ArgumentType<?> previousType(int offset) {
        if (offset >= this.previous_.length) {
            throw new IndexOutOfBoundsException(offset);
        }
        return this.previous_[this.previous_.length - offset - 1];
    }
    public Optional<ArgumentType<?>> previousType() {
        if (this.previous_ == null || this.previous_.length < 1) {
            return Optional.empty();
        }
        return Optional.of(this.previousType(0));
    }
    public boolean hasPrevious() {
        return this.previous_.length > 0;
    }


    //IMPLEMENTATION
    @Override public boolean equals(Object o) {
        if (!super.equals(o)) { return false; }
        if (!(o instanceof ArgumentContext other)) { return false; }
        return     this.commandTokenIndex_ == other.commandTokenIndex_
                && this.command_.equals(other.command_);
    }

    @Override public int hashCode() {
        return super.hashCode() + Objects.hash(this.command_, this.commandTokenIndex_);
    }
}