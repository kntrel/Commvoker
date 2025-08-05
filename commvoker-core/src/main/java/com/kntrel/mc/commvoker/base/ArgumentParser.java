package com.kntrel.mc.commvoker.base;


import com.mojang.brigadier.context.CommandContext;

import java.util.function.BiFunction;

class ArgumentParser<S> {

    //FIELDS
    private final String[] argumentNames_;
    private final BiFunction<CommandContext<? extends S>, Object[], ?> contextualizer_;


    //CONSTRUCTOR
    ArgumentParser(String[] argumentNames, BiFunction<CommandContext<? extends S>, Object[], ?> contextualizer) {
        this.argumentNames_ = argumentNames;
        this.contextualizer_ = contextualizer;
    }


    //UTIL
    Object parse(CommandContext<? extends S> ctx) {
        Object[] objects = new Object[this.argumentNames_.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = ctx.getArgument(this.argumentNames_[i], Object.class);
        }
        return this.contextualizer_.apply(ctx, objects);
    }
}