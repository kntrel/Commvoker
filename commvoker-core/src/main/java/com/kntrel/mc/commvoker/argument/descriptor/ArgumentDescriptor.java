package com.kntrel.mc.commvoker.argument.descriptor;

import com.mojang.brigadier.context.CommandContext;

import java.util.Collection;
import java.util.function.BiFunction;

public interface ArgumentDescriptor<S, T> extends BaseArgumentDescriptor<S> {

    Collection<ArgumentNode<? super S, ?>> argumentNodes();
    BiFunction<CommandContext<S>, Object[], T>  contextualizer();
}
