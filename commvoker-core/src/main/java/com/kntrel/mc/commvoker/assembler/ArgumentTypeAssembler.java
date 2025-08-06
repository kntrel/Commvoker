package com.kntrel.mc.commvoker.assembler;

import com.mojang.brigadier.arguments.ArgumentType;

public interface ArgumentTypeAssembler<T> extends ArgumentType<T>, EndAssembler<T> {

    @Override default ArgumentType<? extends T> argumentType() { return this; }
}
