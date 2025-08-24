package com.kntrel.mc.commvoker.argument.binding;

public interface ArgumentDescriptor<S, T> {

    CommandTemplate<S> template();
    Contextualizer<S, T> contextualizer();
}
