package com.kntrel.mc.commvoker.provided.assemblers;

import com.kntrel.mc.commvoker.argument.assembler.EndAssembler;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

public class IntegerAssembler implements EndAssembler<Integer> {

    //FACTORY
    public static IntegerAssembler integer() {
        return new IntegerAssembler(IntegerArgumentType.integer());
    }
    public static IntegerAssembler integer(int min) {
        return new IntegerAssembler(IntegerArgumentType.integer(min));
    }
    public static IntegerAssembler integer(int min, int max) {
        return new IntegerAssembler(IntegerArgumentType.integer(min, max));
    }


    //FIELDS
    private final IntegerArgumentType argumentType_;


    //CONSTRUCTOR
    private IntegerAssembler(IntegerArgumentType argumentType) {
        this.argumentType_ = argumentType;
    }


    //IMPLEMENTATION
    @Override public ArgumentType<? extends Integer> argumentType() {
        return this.argumentType_;
    }
}
