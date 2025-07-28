package com.kntrel.mc.commvoker.argument.bind;

import com.kntrel.mc.commvoker.argument.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.ParameterContext;

public interface ImplicitArgumentBinding<S, T> extends SimpleArgumentBinding<ParameterContext, T> {

    ArgumentDescriptor.Implicit<S, T> descriptor(ParameterContext ctx);

}
