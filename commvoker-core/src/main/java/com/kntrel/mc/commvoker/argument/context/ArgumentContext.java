package com.kntrel.mc.commvoker.argument.context;

import com.kntrel.mc.commvoker.command.CommandDefinition;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Objects;
import com.kntrel.mc.commvoker.command.CommandToken;

public class ArgumentContext extends ParameterContext {

    //FIELDS
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
        super(parameter, type, method, parameterIndex);
        if (commandTokenIndex < 0) {
            throw new IndexOutOfBoundsException(commandTokenIndex);
        }
        if (commandTokenIndex >= command.size()) {
            throw new IndexOutOfBoundsException(commandTokenIndex);
        }

        this.command_ = Objects.requireNonNull(command, "command");
        this.commandTokenIndex_ = commandTokenIndex;
    }
    public ArgumentContext(ArgumentContext other) {
        this(other.parameter(), other.type(), other.method(), other.parameterIndex(), other.command_, other.commandTokenIndex_);
    }


    //GETTERS
    public CommandDefinition command() { return this.command_; }
    public int commandTokenIndex() { return this.commandTokenIndex_; }


    //UTILITY
    public CommandToken commandToken() {
        return this.command_.getTokenAt(this.commandTokenIndex_);
    }


    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) { return false; }
        if (!(o instanceof ArgumentContext other)) { return false; }
        return this.command_.equals(other.command_) && this.commandTokenIndex_ == other.commandTokenIndex_;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * Objects.hash(command_, commandTokenIndex_);
    }
}