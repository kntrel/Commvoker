package com.kntrel.mc.commvoker.provided.assemblers;

import com.kntrel.mc.commvoker.assembler.EndAssembler;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;

public class BoolAssembler implements EndAssembler<Boolean> {

    //FACTORY
    public static BoolAssembler bool() {
        return new BoolAssembler(BoolArgumentType.bool());
    }


    //FIELDS
    private final BoolArgumentType argumentType_;


    //CONSTRUCTOR
    private BoolAssembler(BoolArgumentType argumentType) {
        this.argumentType_ = argumentType;
    }


    //IMPLEMENTATION
    @Override public ArgumentType<? extends Boolean> argumentType() {
        return this.argumentType_;
    }
}
