package com.kntrel.mc.commvoker.command;

/**
 * Internal API. Public for framework wiring, but not part of the supported external API.
 */
public interface Token {
    String label();
    boolean isLiteral();
    boolean isArgument();
}
