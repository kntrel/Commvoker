package com.kntrel.mc.commvoker.provided.assemblers;

import com.kntrel.mc.commvoker.argument.binding.Components;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import com.mojang.brigadier.context.CommandContext;
import java.lang.reflect.Array;
import java.util.List;

public class ArrayAssembler<S, T> implements TransformAssembler<S, List<T>, T[]> {

    //FACTORY
    public static <S, T> ArrayAssembler<S, T> arrayOf(Class<T> type, Assembler<S, T> delegate) {
        return new ArrayAssembler<>(type, delegate);
    }


    //FIELDS
    private final Class<T> arrayType_;
    private final Assembler<S, T> delegate_;


    //CONSTRUCTOR
    private ArrayAssembler(Class<T> arrayType, Assembler<S, T> delegate) {
        this.arrayType_ = arrayType;
        this.delegate_ = delegate;
    }

    //IMPLEMENTATION
    @Override
    public Assembler<? super S, ? extends List<T>> delegate() {
        return CollectionAssembler.listOf(this.delegate_);
    }

    @Override @SuppressWarnings("unchecked")
    public T[] compose(CommandContext<? extends S> ctx, List<T> upstream) {
        T[] out = (T[]) Array.newInstance(this.arrayType_, upstream.size());
        for (int i = 0; i < out.length; i++) {
            out[i] = upstream.get(i);
        }
        return out;
    }
}
