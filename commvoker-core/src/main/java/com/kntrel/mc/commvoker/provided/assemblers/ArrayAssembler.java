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
        return new ArrayAssembler<>(type, delegate, false);
    }
    public static <S, T> ArrayAssembler<S, T> relaxedArrayOf(Class<T> type, Assembler<S, T> delegate) {
        return new ArrayAssembler<>(type, delegate, true);
    }


    //FIELDS
    private final Class<T> arrayType_;
    private final Assembler<S, T> delegate_;
    private final boolean relaxedMode_;


    //CONSTRUCTOR
    private ArrayAssembler(Class<T> arrayType, Assembler<S, T> delegate, boolean relaxedMode) {
        this.arrayType_ = arrayType;
        this.delegate_ = delegate;
        this.relaxedMode_ = relaxedMode;
    }

    //IMPLEMENTATION
    @Override
    public Assembler<? super S, ? extends List<T>> delegate() {
        return this.relaxedMode_
                ? CollectionAssembler.relaxedListOf(this.delegate_)
                : CollectionAssembler.listOf(this.delegate_);
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
