package com.kntrel.mc.commvoker.argument;

public interface ArgumentResolver<S> {

    ArgumentDescriptor.Parsed<S, ?> resolve(ArgumentContext ctx);
    ArgumentDescriptor.Virtual<S, ?> resolveVirtual(ParameterContext ctx);

}
