package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.binding.Components;
import com.mojang.brigadier.context.CommandContext;

public non-sealed interface ComposedAssembler<S, T> extends Assembler<S, T> {

    void composedOf(AssemblerHook<S> hooK);
}
