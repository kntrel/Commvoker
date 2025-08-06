package com.kntrel.mc.commvoker.argument;

import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;

public interface ArgumentResolver<S> {

    ArgumentDescriptor<? super S, ?> resolve(ArgumentContext ctx);

}
