package com.kntrel.mc.commvoker.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;

public class ByteAssembler implements TransformAssembler<Object, Integer, Byte> {

    public static ByteAssembler byteArg() {
        return new ByteAssembler(IntegerAssembler.integer(Byte.MIN_VALUE, Byte.MAX_VALUE));
    }

    private final IntegerAssembler delegate_;


    private ByteAssembler(IntegerAssembler delegate) {
        this.delegate_ = delegate;
    }


    @Override
    public Assembler<? super Object, ? extends Integer> delegate() {
        return this.delegate_;
    }

    @Override
    public Byte compose(ExecutionContext<?> ctx, Integer object) {
        return object.byteValue();
    }
}
