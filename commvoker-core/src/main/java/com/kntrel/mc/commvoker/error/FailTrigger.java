package com.kntrel.mc.commvoker.error;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.exception.CommandMethodRunException;
import com.kntrel.mc.commvoker.exception.FailedCommandException;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;

public class FailTrigger {

    //FIELDS
    private final ExecutionContext<?> executionContext_;


    //CONSTRUCTORS
    public FailTrigger(ExecutionContext<?> executionContext) {
        this.executionContext_ = executionContext;
    }


    //UTILITY
    public void fail(Message message) throws FailedCommandException {
        throw new CommandMethodRunException(this.executionContext_, message);
    }
    public void fail(String message) throws FailedCommandException {
        this.fail(new LiteralMessage(message));
    }
    public void fail(Throwable error) throws FailedCommandException {
        throw new CommandMethodRunException(this.executionContext_, error.getMessage(), error);
    }
}
