package com.kntrel.mc.commvoker.argument.descriptor;

import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;

public interface TemplatedArgumentDescriptor<S, T> extends ArgumentDescriptor<S, T> {

    CommandTemplate<S> template();
}
