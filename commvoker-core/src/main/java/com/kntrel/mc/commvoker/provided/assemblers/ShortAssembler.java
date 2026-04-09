package com.kntrel.mc.commvoker.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;

public class ShortAssembler implements TransformAssembler<Object, Integer, Short> {

    public static ShortAssembler shortArg() {
        return new ShortAssembler(IntegerAssembler.integer(Short.MIN_VALUE, Short.MAX_VALUE));
    }

    private final IntegerAssembler delegate_;


    private ShortAssembler(IntegerAssembler delegate) {
        this.delegate_ = delegate;
    }


    @Override
    public Assembler<? super Object, ? extends Integer> delegate() {
        return this.delegate_;
    }

    @Override
    public Short compose(ExecutionContext<?> ctx, Integer object) {
        return object.shortValue();
    }
}
