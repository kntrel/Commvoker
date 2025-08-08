package com.kntrel.mc.commvoker.command;

public class CommandPatternToken implements Token {

    //ASSETS
    public enum Type { LITERAL, ARGUMENT, UNNAMED_ARGUMENT, WILDCARD }
    private static final CommandPatternToken
        UNNAMED_ARGUMENT = new CommandPatternToken(null, Type.UNNAMED_ARGUMENT),
        WILDCARD = new CommandPatternToken(null, Type.WILDCARD);


    //FABRIC
    public static CommandPatternToken literal(String label) {
        return new CommandPatternToken(label, Type.LITERAL);
    }
    public static CommandPatternToken argument(String label) {
        return new CommandPatternToken(label, Type.ARGUMENT);
    }
    public static CommandPatternToken argument() {
        return UNNAMED_ARGUMENT;
    }
    public static CommandPatternToken wildcard() {
        return WILDCARD;
    }


    //FIELDS
    private final String label_;
    private final Type type_;


    //CONSTRUCTORS
    private CommandPatternToken(String label, Type type) {
        this.label_ = label;
        this.type_ = type;
    }


    //GETTERS
    public String label() {
        return this.label_;
    }
    public Type type() {
        return this.type_;
    }
    public boolean isLiteral() {
        return this.type_.equals(Type.LITERAL); }
    public boolean isArgument() {
        return this.type_.equals(Type.ARGUMENT) || this.type_.equals(Type.UNNAMED_ARGUMENT);
    }
    public boolean isWildcard() {
        return this.type_.equals(Type.WILDCARD);
    }
    public boolean isLabeled() {
        return this.type_.equals(Type.LITERAL) || this.type_.equals(Type.ARGUMENT);
    }


    //IMPLEMENTATION
    @Override public String toString() {
        if (this.isLiteral()) {
            return this.label();
        }
        if (this.isWildcard()) {
            return "*";
        }
        String label = this.label_;
        if (label == null) { label = ""; }
        return "<" + label + ">";
    }
    @Override public boolean equals(Object o) {
        if (o == null) { return false; }
        if (o == this) { return true; }
        if (!(o instanceof CommandPatternToken other)) { return false; }
        if (!this.type_.equals(other.type_)) { return false; }
        if (this.label_ != null && other.label_ != null) {
            return this.label_.equals(other.label_);
        }
        return this.label_ == null && other.label_ == null;
    }
    @Override public int hashCode() {
        return this.type_.hashCode() * ((this.label_ == null) ? 1 : this.label_.hashCode());
    }
}
