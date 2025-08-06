package com.kntrel.mc.commvoker.provided.assemblers;

import com.kntrel.mc.commvoker.assembler.EndAssembler;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;

public class DoubleAssembler implements EndAssembler<Double> {

    //FACTORY
    public static DoubleAssembler doubleArg() {
        return new DoubleAssembler(DoubleArgumentType.doubleArg());
    }
    public static DoubleAssembler doubleArg(double min) {
        return new DoubleAssembler(DoubleArgumentType.doubleArg(min));
    }
    public static DoubleAssembler doubleArg(double min, double max) {
        return new DoubleAssembler(DoubleArgumentType.doubleArg(min, max));
    }


    //FIELDS
    private final DoubleArgumentType argumentType_;


    //CONSTRUCTOR
    private DoubleAssembler(DoubleArgumentType argumentType) {
        this.argumentType_ = argumentType;
    }


    //IMPLEMENTATION
    @Override public ArgumentType<? extends Double> argumentType() {
        return this.argumentType_;
    }
}