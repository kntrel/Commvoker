package com.kntrel.mc.commvoker.command;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;

public class CommandMethod extends CommandDefinition {

    //FIELDS
    private final Method method_;
    private final Object commandHolder_;
    private final CommandPattern pattern_;


    //CONSTRUCTORS
    public CommandMethod(CommandToken[] tokens, Method method, Object commandHolder, CommandPattern pattern) {
        super(tokens);
        this.method_ = method;
        this.commandHolder_ = commandHolder;
        this.pattern_ = pattern;
    }
    public CommandMethod(Collection<CommandToken> tokens, Method method, Object commandHolder, CommandPattern pattern) {
        super(tokens);
        this.method_ = method;
        this.commandHolder_ = commandHolder;
        this.pattern_ = pattern;
    }


    //GETTERS
    public Method getMethod() {
        return this.method_;
    }
    public CommandPattern getPattern() {
        return this.pattern_;
    }
    public Object getCommandHolder() {
        return this.commandHolder_;
    }
    public Class<?> getReturnType() {
        return this.method_.getReturnType();
    }
    public Type getGenericReturnType() {
        return this.method_.getGenericReturnType();
    }
    public int getParameterCount() {
        return this.method_.getParameterCount();
    }
}
