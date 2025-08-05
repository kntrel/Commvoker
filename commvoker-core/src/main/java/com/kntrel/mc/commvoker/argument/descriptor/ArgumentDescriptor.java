package com.kntrel.mc.commvoker.argument.descriptor;

import com.mojang.brigadier.context.CommandContext;

import java.util.Collection;
import java.util.function.BiFunction;

public interface ArgumentDescriptor<S, T> extends BaseArgumentDescriptor<S> {

    Collection<? extends ArgumentNode<? super S, ?>> argumentNodes();
    BiFunction<CommandContext<? extends S>, Object[], T>  contextualizer();

    default boolean isImplicit() {
        return this.argumentNodes().isEmpty();
    }
}
