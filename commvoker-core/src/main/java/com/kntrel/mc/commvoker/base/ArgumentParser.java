package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.binding.Contextualizer;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class ArgumentParser<S> {

    //ASSETS
    private static final Map<String, String> EMPTY = Collections.emptyMap();


    //FIELDS
    private final Map<String, String> namesMap_;
    private final Contextualizer<? super S, ?> contextualizer_;


    //CONSTRUCTORS
    ArgumentParser(Map<String, String> namesMap, Contextualizer<? super S, ?> contextualizer) {
        this.namesMap_ = namesMap;
        this.contextualizer_ = contextualizer;
    }
    ArgumentParser(Function<ExecutionContext<? extends S>, ?> implicitContextualizer) {
        this(EMPTY, implicitContextualizer::apply);
    }


    //UTIL
    Object parse(CommandContext<? extends S> ctx, List<Object> previous, Map<String, Object> bag) {
        Map<String, Object> compMap = new HashMap<>();

        for (var e : this.namesMap_.entrySet()) try {
            Object o = ctx.getArgument(e.getKey(), Object.class);
            compMap.put(e.getValue(), o);
        } catch (IllegalArgumentException ignored) {}

        return this.contextualizer_.contextualize(new ExecutionContext<>(ctx, compMap, previous, bag));
    }
}