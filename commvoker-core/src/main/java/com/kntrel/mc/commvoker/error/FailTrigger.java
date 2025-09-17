package com.kntrel.mc.commvoker.error;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.exception.CommandMethodRunException;
import com.kntrel.mc.commvoker.exception.FailedCommandException;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import java.util.Optional;
import java.util.function.Supplier;

public class FailTrigger {

    //FIELDS
    private final ExecutionContext<?> executionContext_;


    //CONSTRUCTORS
    public FailTrigger(ExecutionContext<?> executionContext) {
        this.executionContext_ = executionContext;
    }


    //UTILITY
    public FailedCommandException failure(Message message) {
        return new CommandMethodRunException(this.executionContext_, message);
    }
    public FailedCommandException failure(String message) {
        return this.failure(new LiteralMessage(message));
    }
    public FailedCommandException failure(Throwable cause, Message message) {
        return new CommandMethodRunException(this.executionContext_, message, cause);
    }
    public FailedCommandException failure(Throwable cause, String message) {
        return this.failure(cause,new LiteralMessage(message));
    }
    public FailedCommandException failure(Throwable cause) {
        String msg = cause.getMessage();
        if (msg == null) { msg = ""; }
        return this.failure(cause, msg);
    }
    public void fail(Message message) throws FailedCommandException {
        throw this.failure(message);
    }
    public void fail(String message) throws FailedCommandException {
        throw this.failure(message);
    }
    public void fail(Throwable cause, Message message) throws FailedCommandException {
        throw this.failure(cause, message);
    }
    public void fail(Throwable cause, String message) throws FailedCommandException {
        throw this.failure(cause, message);
    }
    public void fail(Throwable cause) throws FailedCommandException {
        throw this.failure(cause);
    }
    public void failIf(Supplier<Boolean> check, Message message) throws FailedCommandException {
        if (check.get()) { this.fail(message); }
    }
    public void failIfNot(Supplier<Boolean> check, Message message) throws FailedCommandException {
        if (!check.get()) { this.fail(message); }
    }
    public void failIf(Supplier<Boolean> check, String message) throws FailedCommandException {
        this.failIf(check, new LiteralMessage(message));
    }
    public void failIfNot(Supplier<Boolean> check, String message) throws FailedCommandException {
        this.failIfNot(check, new LiteralMessage(message));
    }
    public <T> T getOrFail(Optional<T> optional, Message message) throws FailedCommandException {
        T val = optional.orElse(null);
        if (val == null) { this.fail(message); }
        return val;
    }
    public <T> T getOrFail(Optional<T> optional, String message) throws FailedCommandException {
        return this.getOrFail(optional, new LiteralMessage(message));
    }
}
