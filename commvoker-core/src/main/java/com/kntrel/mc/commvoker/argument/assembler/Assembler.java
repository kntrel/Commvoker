package com.kntrel.mc.commvoker.argument.assembler;

public sealed interface Assembler<S, T> permits EndAssembler, ComposedAssembler {

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
