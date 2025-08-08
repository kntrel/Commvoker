package com.kntrel.mc.commvoker.command;

import java.util.Objects;

public class CommandToken implements Token {

    //ASSETS
    public enum Type { LITERAL, ARGUMENT }


    //FABRIC
    public static CommandToken literal(String label) {
        return new CommandToken(label, Type.LITERAL);
    }
    public static CommandToken argument(String label) {
        return new CommandToken(label, Type.ARGUMENT);
    }


    //FIELDS
    private final String label_;
    private final Type type_;


    //CONSTRUCTORS
    public CommandToken(String label, Type type) {
        this.label_ = Objects.requireNonNull(label, "label");
        this.type_ = (type == null) ? Type.LITERAL : type;
    }


    //GETTERS
    public String label() {
        return this.label_;
    }
    public Type type() {
        return this.type_;
    }
    public boolean isLiteral() {
        return this.type_.equals(Type.LITERAL);
    }
    public boolean isArgument() {
        return this.type_.equals(Type.ARGUMENT);
    }


    //IMPLEMENTATION
    @Override public String toString() {
        if (this.isLiteral()) {
            return this.label();
        }
        return "<" + this.label_ + ">";
    }
    @Override public boolean equals(Object o) {
        if (o == null) { return false; }
        if (o == this) { return true; }
        if (!(o instanceof CommandToken other)) { return false; }
        return this.label_.equals(other.label_) && this.type_.equals(other.type_);
    }
    @Override public int hashCode() {
        return Objects.hash(this.label_, this.type_);
    }
}
