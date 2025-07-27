package com.kntrel.mc.commvoker.exception;

import java.lang.reflect.Method;

public class BadCommandMethodException extends Exception {

    private final Method method_;

    public BadCommandMethodException(Method method) {
        this(method, null, null);
    }
    public BadCommandMethodException(Method method, Exception cause) {
        this(method, null, cause);
    }
    public BadCommandMethodException(Method method, String msg) {
        this(method, msg, null);
    }
    public BadCommandMethodException(Method method, String msg, Exception cause) {
        super(msg, cause);
        this.method_ = method;
    }


    public Method getMethod() {
        return method_;
    }
    @Override public String getMessage() {
        String msg = this.getMessageInner();
        if (msg == null || msg.isEmpty()) {
            msg = "Bad command method";
        }
        Method method = this.getMethod();
        return msg + "\n Method: " + method.getDeclaringClass().getName() + "#" + this.getMethod().getName();
    }

    private String getMessageInner() {
        String msg = super.getMessage();
        if (msg != null) { return msg; }
        Throwable cause = this.getCause();
        if (cause != null) { return cause.getMessage(); }
        return null;
    }
}
