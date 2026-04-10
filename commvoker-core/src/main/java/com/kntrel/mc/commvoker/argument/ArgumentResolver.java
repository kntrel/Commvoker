package com.kntrel.mc.commvoker.argument;

import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.context.ArgumentContext;
import com.kntrel.mc.commvoker.argument.context.ParameterContext;
import com.kntrel.mc.commvoker.argument.descriptor.TemplatedArgumentDescriptor;

/**
 * Internal API. Public for framework wiring, but not part of the supported external API.
 */
public interface ArgumentResolver<S> {

    TemplatedArgumentDescriptor<? super S, ?> resolve(ArgumentContext ctx);
    ArgumentDescriptor<? super S, ?> resolve(ParameterContext ctx);
}
