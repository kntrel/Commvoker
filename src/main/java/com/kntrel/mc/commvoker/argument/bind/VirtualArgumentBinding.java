package com.kntrel.mc.commvoker.argument.bind;

import com.kntrel.mc.commvoker.argument.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.ParameterContext;

public interface VirtualArgumentBinding<S, T> extends SimpleArgumentBinding<ParameterContext, T> {

    ArgumentDescriptor.Virtual<S, T> descriptor(ParameterContext ctx);

}
