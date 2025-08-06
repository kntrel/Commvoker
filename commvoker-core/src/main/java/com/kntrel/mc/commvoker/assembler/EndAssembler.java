package com.kntrel.mc.commvoker.assembler;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public non-sealed interface EndAssembler<T> extends Assembler<Object, T>{

    ArgumentType<? extends T> argumentType();

    @Override
    default boolean isImplicit() {
        return false;
    }

    @Override
    default Object[] parseRaw(StringReader reader) throws CommandSyntaxException {
        return new Object[]{ this.argumentType().parse(reader) };
    }
}
