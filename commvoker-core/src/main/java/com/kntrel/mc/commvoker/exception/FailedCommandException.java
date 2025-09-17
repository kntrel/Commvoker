package com.kntrel.mc.commvoker.exception;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

public class FailedCommandException extends CommandSyntaxException {
    //ASSETS
    private static final DynamicCommandExceptionType EXCEPTION_TYPE = new DynamicCommandExceptionType(o -> (o instanceof Message m) ? m : new LiteralMessage(o.toString()));


    //FIELDS
    private final Object source_;
    private final Throwable cause_;


    //CONSTRUCTORS
    public FailedCommandException(Object source, Message message, Throwable cause) {
        super(EXCEPTION_TYPE, message);
        this.source_ = source;
        this.cause_ = cause;
    }
    public FailedCommandException(Object source, String message, Throwable cause) {
        this(source, new LiteralMessage(message), cause);
    }
    public FailedCommandException(Object source, Message message) {
        this(source, message, null);
    }
    public FailedCommandException(Object source, String message) {
        this(source, message, null);
    }

    //GETTERS
    @Override public Throwable getCause() {
        return this.cause_;
    }
    public Object getSource() {
        return this.source_;
    }
}
