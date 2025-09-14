package com.kntrel.mc.commvoker.argument.descriptor;

import com.kntrel.mc.commvoker.argument.binding.Contextualizer;
import java.util.function.Predicate;

public interface ArgumentDescriptor<S, T> {

    Contextualizer<S, T> contextualizer();
    Predicate<S> requirement();
}
