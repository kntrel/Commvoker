package com.kntrel.mc.commvoker.base;


import com.kntrel.mc.commvoker.argument.binding.Components;
import com.kntrel.mc.commvoker.argument.binding.Contextualizer;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    ArgumentParser(Function<CommandContext<? extends S>, ?> implicitContextualizer) {
        this(EMPTY, (ctx, comp) -> implicitContextualizer.apply(ctx));
    }


    //UTIL
    Object parse(CommandContext<? extends S> ctx) {
        Map<String, Object> compMap = this.namesMap_.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getValue,
                e -> ctx.getArgument(e.getKey(), Object.class)
        ));
        return this.contextualizer_.contextualize(ctx, new Components(compMap));
    }
}