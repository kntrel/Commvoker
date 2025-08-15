package com.kntrel.mc.commvoker.argument;

import com.kntrel.mc.commvoker.argument.binding.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.context.ArgumentContext;
import com.kntrel.mc.commvoker.argument.context.ParameterContext;
import com.mojang.brigadier.context.CommandContext;
import java.util.function.Function;

public interface ArgumentResolver<S> {

    ArgumentDescriptor<? super S, ?> resolve(ArgumentContext ctx);
    Function<CommandContext<? extends S>, ?> resolve(ParameterContext ctx);

}
