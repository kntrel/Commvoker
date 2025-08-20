package com.kntrel.mc.commvoker.argument.binding;

import java.util.Collection;

public interface ArgumentDescriptor<S, T> {

    CommandTemplate.Node<S> argumentTrees();
    Contextualizer<S, T> contextualizer();
}
