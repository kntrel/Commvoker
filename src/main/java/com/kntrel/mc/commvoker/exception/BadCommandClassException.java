package com.kntrel.mc.commvoker.exception;

public class BadCommandClassException extends RuntimeException {

    private final Object instance_;

    public BadCommandClassException(Object instance, Exception cause) {
        super(cause);
        this.instance_ = instance;
    }
    public BadCommandClassException(Object instance_, String msg) {
        super(msg);
        this.instance_ = instance_;
    }


    public Object getInstance() {
        return this.instance_;
    }
}
