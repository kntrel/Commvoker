package com.kntrel.mc.commvoker.argument.assembler;

import com.mojang.brigadier.arguments.ArgumentType;

public non-sealed interface EndAssembler<T> extends Assembler<Object, T>{

    ArgumentType<? extends T> argumentType();

    @Override
    default boolean isImplicit() {
        return false;
    }
}
