package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.binding.Suggester;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.argument.descriptor.InstancedArgumentDescriptor;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class SuggesterBridge<S> implements SuggestionProvider<S> {

    private final Suggester<S> suggester_;
    private final ArgumentParser<S>[] parsers_;


    //CONSTRUCTOR
    SuggesterBridge(Suggester<S> suggester, ArgumentParser<S>[] parsers) {
        this.suggester_ = suggester;
        this.parsers_ = parsers;
    }


    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Map<String, Object> bag = new HashMap<>();
        List<InstancedArgumentDescriptor<S, ?>> descriptors = new ArrayList<>();
        for (ArgumentParser<S> parser : this.parsers_) try {
            InstancedArgumentDescriptor<S, ?> desc = parser.parse(context, descriptors, bag);
            descriptors.add(desc);
        } catch (Throwable ignored) { break; }

        ExecutionContext<S> execCtx = new ExecutionContext<>(context, Map.of(), descriptors, bag);
        return this.suggester_.suggest(execCtx, builder);
    }
}
