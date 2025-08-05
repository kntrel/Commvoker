package com.kntrel.mc.commvoker.provided.assemblers;

import com.kntrel.mc.commvoker.assembler.EndAssembler;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;

public class LongAssembler implements EndAssembler<Long> {

    //FACTORY
    public static LongAssembler longArg() {
        return new LongAssembler(LongArgumentType.longArg());
    }
    public static LongAssembler longArg(long min) {
        return new LongAssembler(LongArgumentType.longArg(min));
    }
    public static LongAssembler longArg(long min, long max) {
        return new LongAssembler(LongArgumentType.longArg(min, max));
    }


    //FIELDS
    private final LongArgumentType argumentType_;


    //CONSTRUCTOR
    private LongAssembler(LongArgumentType argumentType) {
        this.argumentType_ = argumentType;
    }


    //IMPLEMENTATION
    @Override public ArgumentType<? extends Long> argumentType() {
        return this.argumentType_;
    }

}
