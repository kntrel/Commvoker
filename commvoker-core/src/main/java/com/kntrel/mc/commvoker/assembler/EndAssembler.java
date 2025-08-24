package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;


public non-sealed interface EndAssembler<S, T> extends Assembler<S, T>{

    CommandTemplate<S> argumentTemplate();
}
