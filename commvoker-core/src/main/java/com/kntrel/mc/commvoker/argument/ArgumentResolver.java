package com.kntrel.mc.commvoker.argument;

public interface ArgumentResolver<S> {

    ArgumentDescriptor.Parsed<S, ?> resolve(ArgumentContext ctx);
    ArgumentDescriptor.Implicit<S, ?> resolveImplicit(ParameterContext ctx);

}
