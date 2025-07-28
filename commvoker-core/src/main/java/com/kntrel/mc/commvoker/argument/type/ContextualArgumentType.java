package com.kntrel.mc.commvoker.argument.type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public interface ContextualArgumentType<S, I, T> extends ArgumentType<I>, Contextualizer<S, I, T> {

    default T parse(CommandContext<S> context, StringReader reader) throws CommandSyntaxException {
        I intermediate = this.parse(reader);
        return this.contextualize(context, intermediate);
    }

}
