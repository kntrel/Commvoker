package com.kntrel.mc.commvoker.exception;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import java.lang.reflect.Method;

public class CommandMethodRunException extends FailedCommandException {

    //FIELDS
    private final ExecutionContext<?> executionContext_;


    //CONSTRUCTORS
    public CommandMethodRunException(ExecutionContext<?> executionContext, Message message, Throwable cause) {
        super(executionContext.source(), message, cause);
        this.executionContext_ = executionContext;
    }
    public CommandMethodRunException(ExecutionContext<?> executionContext, String message, Throwable cause) {
        this(executionContext, new LiteralMessage(message), cause);
    }
    public CommandMethodRunException(ExecutionContext<?> executionContext, Message message) {
        this(executionContext, message, null);
    }
    public CommandMethodRunException(ExecutionContext<?> executionContext, String message) {
        this(executionContext, new LiteralMessage(message));
    }


    //GETTERS
    public ExecutionContext<?> getExecutionContext() {
        return this.executionContext_;
    }
    public Method getCommandMethod() {
        return this.executionContext_.method();
    }
    public Object getCommandHolder() {
        return this.executionContext_.commandHolder();
    }
    public Object getSource() {
        return this.executionContext_.source();
    }
}
