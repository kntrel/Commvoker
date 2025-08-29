package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.mojang.brigadier.arguments.ArgumentType;

public interface ArgumentTypeAssembler<T> extends EndAssembler<Object, T> {

    ArgumentType<? extends T> argumentType();

    @Override
    default CommandTemplate<Object> argumentTemplate() {
        return CommandTemplate.argument("arg", this.argumentType()).end();
    }

    @Override @SuppressWarnings("unchecked")
    default T contextualize(ExecutionContext<?> ctx) {
        return (T) ctx.component("arg");
    }
}