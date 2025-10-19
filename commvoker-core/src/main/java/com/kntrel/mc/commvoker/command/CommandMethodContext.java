package com.kntrel.mc.commvoker.command;

import com.kntrel.util.TypeUtils;
import com.mojang.brigadier.context.CommandContext;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class CommandMethodContext<S> {

    //FIELDS
    private final CommandMethod commandMethod_;
    private final CommandContext<S> commandContext_;
    private final List<CommandMethodArgument> arguments_;
    private final List<CommandMethodArgument.Explicit> commandArguments_;
    private final List<CommandMethodArgument.Implicit> implicitArguments_;
    private final Map<String, Object> bag_;


    //CONSTRUCTORS
    public CommandMethodContext(
            CommandMethod commandMethod,
            CommandContext<S> commandContext,
            Collection<? extends CommandMethodArgument> arguments,
            Map<String, Object> bag
    ) {
        this.commandMethod_ = commandMethod;
        this.commandContext_ = commandContext;
        this.arguments_ = new ArrayList<>();
        this.commandArguments_ = new ArrayList<>();
        this.implicitArguments_ = new ArrayList<>();
        this.bag_ = bag;

        this.populateArguments(arguments);
    }


    public CommandMethod commandMethod() {
        return this.commandMethod_;
    }
    public CommandContext<S> commandContext() {
        return this.commandContext_;
    }

    public Object methodArgument(int index) {
        return this.arguments_.get(index).value();
    }
    public <T> T methodArgument(int index, Class<T> type) {
        return ensureType(this.methodArgument(index), type);
    }
    public Object commandArgument(int index) {
        return this.commandArguments_.get(index).value();
    }
    public <T> T commandArgument(int index, Class<T> type) {
        return ensureType(this.commandArgument(index), type);
    }
    public Object commandArgument(String name) {
        for (CommandMethodArgument.Explicit arg : this.commandArguments_) {
            if (arg.name().equals(name)) {
                return arg.value();
            }
        }
        return null;
    }
    public <T> T commandArgument(String name, Class<T> type) {
        return ensureType(this.commandArgument(name), type);
    }
    public List<CommandMethodArgument> arguments() {
        return Collections.unmodifiableList(this.arguments_);
    }
    public List<CommandMethodArgument.Explicit> commandArguments() {
        return Collections.unmodifiableList(this.commandArguments_);
    }
    public List<CommandMethodArgument.Implicit> implicitArguments() {
        return Collections.unmodifiableList(this.implicitArguments_);
    }

    public Object bagObject(String key) {
        return this.bag_.get(key);
    }
    public <T> T bagObject(String key, Class<T> type) {
        return ensureType(this.bagObject(key), type);
    }
    public boolean hasBagObject(String key) {
        return this.bag_.containsKey(key);
    }


    //SHORTHANDS
    public S source() { return this.commandContext().getSource(); }
    public Object commandHolder() { return this.commandMethod().getCommandHolder(); }
    public Method method() { return this.commandMethod().getMethod(); }


    //HELPERS
    private void populateArguments(Collection<? extends CommandMethodArgument> arguments) {
        int paramCount = this.commandMethod_.getParameterCount();
        if (arguments.size() < paramCount) {
            throw new IllegalArgumentException("Not enough arguments to populate method parameters");
        }
        List<CommandMethodArgument> sorted = arguments.stream().map(a -> (CommandMethodArgument)a).sorted().toList();
        List<CommandToken> argTokens = new ArrayList<>();
        for (CommandToken t : this.commandMethod_) {
            if (t.isArgument()) { argTokens.add(t); }
        }


        int commandIndex = 0;
        for (int i = 0; i < paramCount; i++) {
            CommandMethodArgument arg = sorted.get(i);
            if (i != arg.methodIndex()) {
                throw new IllegalArgumentException("Argument method index mismatch at parameter index " + i);
            }

            Object val = arg.value();
            Parameter param = this.commandMethod_.getMethod().getParameters()[i];

            if (!TypeUtils.isAssignableFrom(param.getType(), val.getClass())) {
                throw new IllegalArgumentException("Argument type mismatch at parameter index " + i + ": expected " + param.getType().getName() + ", got " + val.getClass().getName());
            }

            switch (arg) {
                case CommandMethodArgument.Implicit imp -> {
                    this.implicitArguments_.add(imp);
                    this.arguments_.add(imp);
                }
                case CommandMethodArgument.Explicit exp -> {
                    if (commandIndex >= argTokens.size()) {
                        throw new IllegalArgumentException("Explicit argument index out of bounds for command tokens");
                    }
                    CommandToken tok = argTokens.get(commandIndex++);
                    if (!tok.label().equals(exp.name())) {
                        exp = new CommandMethodArgument.Explicit(i, val, tok.label());
                    }

                    this.commandArguments_.add(exp);
                    this.arguments_.add(exp);
                }
            }
        }

        if (commandIndex != argTokens.size()) {
            throw new IllegalArgumentException("Not all command tokens matched with explicit arguments");
        }
    }
    private static <T> T ensureType(Object obj, Class<T> type) {
        if (obj == null) { return null; }
        if (!type.isAssignableFrom(obj.getClass())) {
            throw new ClassCastException("Object of type '" + obj.getClass().getName() + "' is not compatible with '" + type.getName() + "'");
        }
        return type.cast(obj);
    }
}
