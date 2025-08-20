package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.argument.binding.Components;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;

public interface ArgumentTypeAssembler<T> extends EndAssembler<Object, T> {

    ArgumentType<? extends T> argumentType();

    @Override
    default CommandTemplate.Node<Object> argumentTemplate() {
        return CommandTemplate.beginArgument("arg", this.argumentType()).end();
    }

    @Override @SuppressWarnings("unchecked")
    default T contextualize(CommandContext<?> context, Components components) {
        return (T) components.get("arg");
    }

}
