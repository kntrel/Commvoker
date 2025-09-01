package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.binding.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.argument.binding.Contextualizer;
import com.kntrel.mc.commvoker.argument.binding.Suggester;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.argument.descriptor.InstancedArgumentDescriptor;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.lang.reflect.Type;
import java.util.*;
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
        ArgumentParser<S> lastParser = null;
        for (ArgumentParser<S> parser : this.parsers_) {
            if (!parser.canParse(context)) {
                lastParser = parser;
                break;
            }

            List<InstancedArgumentDescriptor<S, ?>> copy = List.copyOf(descriptors);
            InstancedArgumentDescriptor<S, ?> desc = new LazyArgumentDescriptor<>(parser, context, copy, bag);
            descriptors.add(desc);
        }

        Map<String, Object> compMap = (lastParser != null) ? lastParser.components(context) : Collections.emptyMap();
        ExecutionContext<S> execCtx = new ExecutionContext<>(context, compMap, descriptors, bag);
        return this.suggester_.suggest(execCtx, builder);
    }


    private static class LazyArgumentDescriptor<S, T> implements InstancedArgumentDescriptor<S, T> {

        //FIELDS
        private final ArgumentDescriptor<S, T> delegate_;
        private final ArgumentParser<S> parser_;
        private final CommandContext<S> context_;
        private final List<InstancedArgumentDescriptor<S, ?>> previous_;
        private final Map<String, Object> bag_;
        private InstancedArgumentDescriptor<S, T> cached_ = null;


        //CONSTRUCTOR
        LazyArgumentDescriptor(
                ArgumentParser<S> parser,
                CommandContext<S> context,
                List<InstancedArgumentDescriptor<S, ?>> previous,
                Map<String, Object> bag
        ) {
            this.delegate_ = (ArgumentDescriptor<S, T>) parser.argumentDescriptor();
            this.parser_ = parser;
            this.context_ = context;
            this.previous_ = previous;
            this.bag_ = bag;
        }

        //IMPLEMENTATION
        @Override public CommandTemplate<S> template() { return this.delegate_.template(); }
        @Override public Contextualizer<S, T> contextualizer() { return this.delegate_.contextualizer(); }
        @Override public Type type() { return this.resolve().type(); }
        @Override public T value() { return this.resolve().value(); }

        //PRIVATE
        InstancedArgumentDescriptor<S, T> resolve() {
            if (this.cached_ != null) {
                return this.cached_;
            }
            InstancedArgumentDescriptor<S, ?> desc = this.parser_.parse(this.context_, this.previous_, this.bag_);
            this.cached_ = (InstancedArgumentDescriptor<S, T>) desc;
            return this.cached_;
        }
    }
}
