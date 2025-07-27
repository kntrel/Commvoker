package com.kntrel.mc.commvoker.command;

public interface Token {
    String label();
    boolean isLiteral();
    boolean isArgument();
}
