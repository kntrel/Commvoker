package com.kntrel.mc.commvoker.argument.type;

import com.mojang.brigadier.arguments.ArgumentType;

@FunctionalInterface
public non-sealed interface RawArgumentType<T> extends Producer<T> {

    ArgumentType<T> value();

}
