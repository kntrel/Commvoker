package com.kntrel.mc.commvoker.provided.assemblers;

import com.kntrel.mc.commvoker.assembler.ArgumentTypeAssembler;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;

public class FloatAssembler implements ArgumentTypeAssembler<Float> {

    //FACTORY
    public static FloatAssembler floatArg() {
        return new FloatAssembler(FloatArgumentType.floatArg());
    }
    public static FloatAssembler floatArg(float min) {
        return new FloatAssembler(FloatArgumentType.floatArg(min));
    }
    public static FloatAssembler floatArg(float min, float max) {
        return new FloatAssembler(FloatArgumentType.floatArg(min, max));
    }


    //FIELDS
    private final FloatArgumentType floatArgumentType_;


    //CONSTRUCTOR
    private FloatAssembler(FloatArgumentType floatArgumentType) {
        this.floatArgumentType_ = floatArgumentType;
    }


    //IMPLEMENTATION
    @Override public ArgumentType<? extends Float> argumentType() {
        return this.floatArgumentType_;
    }
}
