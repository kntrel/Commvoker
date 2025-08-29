package com.kntrel.mc.commvoker.assembler;

public non-sealed interface ComposedAssembler<S, T> extends Assembler<S, T> {

    void composedOf(AssemblerHook<S> hooK);
}
