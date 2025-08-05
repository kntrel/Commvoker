package com.kntrel.mc.commvoker.provided.assemblers;

import com.kntrel.mc.commvoker.assembler.EndAssembler;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

public class StringAssembler implements EndAssembler<String> {

    //FACTORY
    public static StringAssembler string() {
        return new StringAssembler(StringArgumentType.string());
    }
    public static StringAssembler word() {
        return new StringAssembler(StringArgumentType.word());
    }
    public static StringAssembler greedyString() {
        return new StringAssembler(StringArgumentType.greedyString());
    }



    //FIELDS
    private final StringArgumentType delegate_;


    //CONSTRUCTORS
    private StringAssembler(StringArgumentType delegate) {
        this.delegate_ = delegate;
    }


    //IMPLEMENTATION
    @Override public ArgumentType<? extends String> argumentType() {
        return this.delegate_;
    }

}
