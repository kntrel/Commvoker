package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.mojang.brigadier.arguments.ArgumentType;

public sealed interface Assembler<S, T> permits EndAssembler, ComposedAssembler {

    static <T> EndAssembler<Object, T> ofArgumentType(ArgumentType<T> argumentType) {
        return (ArgumentTypeAssembler<T>) () -> argumentType;
    }

    static <S, T> Assembler<S, T> ofArgumentDescriptor(ArgumentDescriptor<S, T> argumentDescriptor) {
        return ArgumentDescriptorAssembler.argumentDescriptor(argumentDescriptor);
    }


    T assemble(ExecutionContext<? extends S> context) throws AssemblyException;

    default CompiledAssembler<S, T> compile() {
        return CompiledAssembler.of(this);
    }
}
