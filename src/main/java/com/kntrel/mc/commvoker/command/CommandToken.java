package com.kntrel.mc.commvoker.command;

public record CommandToken(String label, Type type) {
    public enum Type { LITERAL, ARGUMENT }

    public boolean isLiteral() { return type.equals(Type.LITERAL); }
    public boolean isArgument() { return type.equals(Type.ARGUMENT); }

    @Override public String toString() {
        if (this.isLiteral()) {
            return this.label();
        }
        return "<" + this.label() + ">";
    }
}
