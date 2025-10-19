package com.kntrel.mc.commvoker.command;

public sealed interface CommandMethodArgument extends Comparable<CommandMethodArgument> {

    Object value();
    int methodIndex();
    @Override default int compareTo(CommandMethodArgument o) {
        return Integer.compare(this.methodIndex(), o.methodIndex());
    }


    record Implicit(int methodIndex, Object value) implements CommandMethodArgument {}
    record Explicit(int methodIndex, Object value, String name) implements CommandMethodArgument {}

}
