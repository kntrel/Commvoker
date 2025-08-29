package com.kntrel.mc.commvoker.argument.context;

import com.kntrel.mc.commvoker.argument.binding.ArgumentDescriptor;
import com.kntrel.mc.commvoker.command.CommandDefinition;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import com.kntrel.mc.commvoker.command.CommandToken;
import com.kntrel.util.tuple.Pair;

public class ArgumentContext extends ParameterContext {

    //FIELDS
    private final CommandDefinition command_;
    private final int commandTokenIndex_;
    private final List<Pair<Type, ArgumentDescriptor<?, ?>>> previous_;


    //CONSTRUCTORS
    public ArgumentContext(
            Parameter parameter,
            Type type,
            Method method,
            int parameterIndex,
            CommandDefinition command,
            int commandTokenIndex,
            SequencedCollection<Pair<Type, ArgumentDescriptor<?, ?>>> previous
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
        this.previous_ = List.copyOf(previous);
    }
    public ArgumentContext(ArgumentContext other) {
        this(other.parameter(), other.type(), other.method(), other.parameterIndex(), other.command_, other.commandTokenIndex_, other.previous_);
    }


    //GETTERS
    public CommandDefinition command() { return this.command_; }
    public int commandTokenIndex() { return this.commandTokenIndex_; }
    public List<Pair<Type, ArgumentDescriptor<?, ?>>> previous() {
        return this.previous_;
    }
    public Pair<Type, ArgumentDescriptor<?, ?>> previous(int back) {
        if (back < 0) {
            throw new IndexOutOfBoundsException("Previous item offset must be >= 0. Provided: " + back);
        }
        int i = this.previous_.size() - 1 - back;
        if (i < 0) { i = 0; }
        return this.previous_.get(i);
    }
    public Set<Class<?>> previousClasses() {
        return this.previous_.stream()
                .map(Pair::first)
                .map(t -> t instanceof Class<?> c ? c : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    public Collection<Type> previousTypes() {
        return this.previous_.stream()
                .map(Pair::first)
                .toList();
    }
    public boolean hasPrevious() {
        return !this.previous_.isEmpty();
    }


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