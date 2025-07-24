package com.kntrel.mc.commvoker.argument;

import java.util.Optional;

public interface ArgumentResolver<S> {

    ArgumentDescriptor<S, ?> resolve(ParameterContext ctx);

}
