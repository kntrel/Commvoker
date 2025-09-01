package com.kntrel.mc.commvoker.base;


import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.argument.descriptor.CompiledArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.descriptor.InstancedArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.descriptor.TypedArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.binding.Suggester;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;

import java.util.*;
import java.util.concurrent.CompletableFuture;

class SuggesterBridge<S> implements SuggestionProvider<S> {

    private final Suggester<S> suggester_;
    private final IdentityHashMap<CommandNode<S>, CompiledArgumentDescriptor<S, ?>> contextMap_;

    public SuggesterBridge(Suggester<S> suggester, IdentityHashMap<CommandNode<S>, CompiledArgumentDescriptor<S, ?>> contextMap) {
        this.suggester_ = suggester;
        this.contextMap_ = contextMap;
    }


    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

        ExecutionContext<S> execCtx = new ExecutionContext<>(context, Collections.emptyMap(), Collections.emptyList(), new HashMap<>());
        List<CommandNode<S>> path = context.getNodes().stream().map(ParsedCommandNode::getNode).toList();
        List<InstancedArgumentDescriptor<S, ?>> descriptors = new ArrayList<>();

        CompiledArgumentDescriptor<S, ?> current = null;
        Collection<CommandNode<S>> upstream = List.of();
        for (CommandNode<S> node : path) {
            if (current == null) {
                current = this.contextMap_.get(node);
                if (current == null) { break; }
                upstream = node.getChildren();
                continue;
            }

            if (!upstream.contains(node)) try {
                Object val = current.contextualizer().contextualize(ExecutionContext.copyOf(execCtx, descriptors));
                descriptors.add(InstancedArgumentDescriptor.of((TypedArgumentDescriptor<S, Object>) current, val));
                current = null;
                continue;
            } catch (Throwable t) {
                break;
            }

            upstream = node.getChildren();
        }

        return this.suggester_.suggest(execCtx, builder);
    }
}
