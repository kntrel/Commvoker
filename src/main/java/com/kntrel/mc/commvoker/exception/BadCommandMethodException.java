package com.kntrel.mc.commvoker.exception;

import java.lang.reflect.Method;

public class BadCommandMethodException extends Exception {

    private final Method method_;
    private final String command_;
    private final int pointer_;
    private String msg_ = null;

    public BadCommandMethodException(Method method, String command, int pointer) {
        this.method_ = method;
        this.command_ = command;
        this.pointer_ = pointer;
    }

    public BadCommandMethodException(Method method, String command, int pointer, String msg) {
        super(msg);
        this.method_ = method;
        this.command_ = command;
        this.pointer_ = pointer;
    }


    public Method getMethod() {
        return method_;
    }
    public String getCommand() {
        return command_;
    }
    public int getPointer() {
        return pointer_;
    }

    @Override public String getMessage() {
        if (this.msg_ != null) {
            return this.msg_;
        }

        StringBuilder msg = new StringBuilder(super.getMessage());
        if (!msg.isEmpty()) {
            msg.append('\n');
        }
        msg.append("> ").append(this.getCommand()).append("\n> ");
        int i = this.getPointer();
        while (i-- > 0) {
            msg.append(' ');
        }

        Method m = this.getMethod();
        msg.append("^")
           .append("\n> at").append(m.getDeclaringClass().getName()).append('#').append(m.getName()).append('\n');

        this.msg_ = msg.toString();
        return this.msg_;
    }
}
