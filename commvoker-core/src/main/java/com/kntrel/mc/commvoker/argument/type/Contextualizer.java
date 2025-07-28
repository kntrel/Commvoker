package com.kntrel.mc.commvoker.argument.type;

import com.mojang.brigadier.context.CommandContext;

public interface Contextualizer<S, I, T> {

    T contextualize(CommandContext<S> context, I subject);

}
