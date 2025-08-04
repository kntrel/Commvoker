package com.kntrel.mc.commvoker.argument.type;

import com.mojang.brigadier.arguments.ArgumentType;

public sealed interface Producer<T>
permits ComposedArgumentType, ContextualArgumentType, RawArgumentType {

    static <T> RawArgumentType<T> ofRaw(ArgumentType<T> argumentType) {
        return () -> argumentType;
    }

}
