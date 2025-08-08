package com.kntrel.mc.commvoker.argument.descriptor;

import java.util.function.Predicate;

public interface BaseArgumentDescriptor<S> {

    Predicate<S> requirement();

}
