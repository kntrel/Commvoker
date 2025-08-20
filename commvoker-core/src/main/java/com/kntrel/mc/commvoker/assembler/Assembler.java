package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.binding.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.argument.binding.Components;
import com.kntrel.mc.commvoker.argument.binding.Contextualizer;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;

public sealed interface Assembler<S, T> extends Contextualizer<S, T> permits EndAssembler, ComposedAssembler {

    static <T> EndAssembler<Object, T> ofArgumentType(ArgumentType<T> argumentType) {
        return (ArgumentTypeAssembler<T>) () -> argumentType;
    }

    static <S, T> Assembler<S, T> ofArgumentDescriptor(ArgumentDescriptor<S, T> argumentDescriptor) {
        return ArgumentDescriptorAssembler.argumentDescriptor(argumentDescriptor);
    }


    default CompiledAssembler<S, T> compile() {
        return CompiledAssembler.of(this);
    }
}
