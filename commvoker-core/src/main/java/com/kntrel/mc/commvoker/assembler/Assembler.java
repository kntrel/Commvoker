package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.*;

public sealed interface Assembler<S, T> permits EndAssembler, ComposedAssembler {

    static <T> EndAssembler<T> ofArgumentType(ArgumentType<T> argumentType) {
        return () -> argumentType;
    }

    static <S, T> Assembler<S, T> ofArgumentDescriptor(ArgumentDescriptor<S, T> argumentDescriptor) {
        return ArgumentDescriptorAssembler.argumentDescriptor(argumentDescriptor);
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

    default Object[] parseRaw(StringReader reader) throws CommandSyntaxException {
        List<Object> out = new ArrayList<>();
        Deque<Assembler<? super S, ?>> stack = new ArrayDeque<>();
        stack.addLast(this);

        while (!stack.isEmpty()) {
            switch (stack.pollLast()) {
                case EndAssembler<?> end -> {
                    reader.skipWhitespace();
                    out.add(end.argumentType().parse(reader));
                }
                case ComposedAssembler<? super S, ?> comp -> {
                    var delegates = comp.delegates();
                    for (int i = delegates.size() - 1; i >= 0; i--) {
                        stack.addLast(delegates.get(i).first());
                    }
                }
            }
        }

        return out.toArray(Object[]::new);
    }
}
