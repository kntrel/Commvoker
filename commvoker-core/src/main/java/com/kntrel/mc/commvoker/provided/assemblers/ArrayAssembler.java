package com.kntrel.mc.commvoker.provided.assemblers;

import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import com.mojang.brigadier.context.CommandContext;
import java.lang.reflect.Array;
import java.util.List;

public class ArrayAssembler<T> implements TransformAssembler<Object, List<T>, T[]> {

    //FACTORY
    public static <T> ArrayAssembler<T> arrayOf(Class<T> type, Assembler<?, T> delegate) {
        return new ArrayAssembler<>(type, delegate);
    }


    //FIELDS
    private final Class<T> arrayType_;
    private final Assembler<?, T> delegate_;


    //CONSTRUCTOR
    private ArrayAssembler(Class<T> arrayType, Assembler<?, T> delegate) {
        this.arrayType_ = arrayType;
        this.delegate_ = delegate;
    }


    //IMPLEMENTATION
    @Override
    public Assembler<? super Object, ? extends List<T>> delegate() {
        return CollectionAssembler.listOf(this.delegate_);
    }

    @Override @SuppressWarnings("unchecked")
    public T[] compose(CommandContext<?> ctx, List<T> upstream) {
        T[] out = (T[]) Array.newInstance(this.arrayType_, upstream.size());
        for (int i = 0; i < out.length; i++) {
            out[i] = upstream.get(i);
        }
        return out;
    }
}
