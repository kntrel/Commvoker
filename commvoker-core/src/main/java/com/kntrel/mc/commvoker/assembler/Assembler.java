package com.kntrel.mc.commvoker.assembler;

import com.mojang.brigadier.arguments.ArgumentType;

public sealed interface Assembler<S, T> permits EndAssembler, ComposedAssembler {

    static <T> EndAssembler<T> ofArgumentType(ArgumentType<T> argumentType) {
        return () -> argumentType;
    }


    default boolean isImplicit() {
        if (this instanceof ComposedAssembler<S,T> c) {
            for (var i : c.delegates()) {
                if (!i.first().isImplicit()) { return false; }
            }
            return true;
        }

        return false;
    }

}
