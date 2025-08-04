package com.kntrel.mc.commvoker.argument.bind;

import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.mc.commvoker.argument.assembler.Assembler;
import com.kntrel.mc.commvoker.argument.binder.ArgumentBinder;
import com.kntrel.mc.commvoker.argument.binder.ArgumentGatherer;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;

import java.util.function.Function;

public interface ArgumentBinding<S, T> extends BaseArgumentBinding<ArgumentContext, T> {

    static <S, T> ArgumentBinder<S, T> ofAssembler(Function<ArgumentGatherer<S>, Assembler<S, T>> supplier) {
        return ArgumentBinder.argumentAssembler(supplier);
    }

    ArgumentDescriptor<S, T> descriptor(ArgumentGatherer<S> ctx);

}
