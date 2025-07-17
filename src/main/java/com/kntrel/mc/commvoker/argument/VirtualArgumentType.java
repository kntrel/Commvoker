package com.kntrel.mc.commvoker.argument;

import com.mojang.brigadier.context.CommandContext;

import java.util.function.Function;

public interface VirtualArgumentType<S, T> extends Function<CommandContext<S>, T> {

    T parse(CommandContext<S> ctx);
    @Override default T apply(CommandContext<S> ctx) {return parse(ctx); }

}
