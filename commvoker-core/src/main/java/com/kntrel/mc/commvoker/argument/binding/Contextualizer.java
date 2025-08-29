package com.kntrel.mc.commvoker.argument.binding;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;

@FunctionalInterface
public interface Contextualizer<S, T> {

    T contextualize(ExecutionContext<? extends S> ctx);

}
