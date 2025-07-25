package com.kntrel.mc.commvoker.argument;

public interface ArgumentResolver<S> {

    ArgumentDescriptor<S> resolve(ArgumentContext ctx);

}
