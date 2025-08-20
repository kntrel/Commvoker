package com.kntrel.mc.commvoker.argument.binding;

import com.mojang.brigadier.context.CommandContext;

@FunctionalInterface
public interface Contextualizer<S, T> {

    T contextualize(CommandContext<? extends S> context, Components components);

}
