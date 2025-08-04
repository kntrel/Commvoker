package com.kntrel.mc.commvoker.argument.type;

import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.mc.commvoker.argument.assembler.TypeUtils;
import com.kntrel.util.tuple.Pair;
import com.kntrel.util.tuple.impl.SimplePair;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AliasArgumentType<S, I, T> extends ComposedArgumentType<S, T>, SuggestionProvider<S> {

    Producer<I> delegate(ArgumentContext ctx);

    T map(CommandContext<S> ctx, I subject);

    default boolean suggests() {
        return TypeUtils.hasMethod(this.getClass(), "getSuggestions", CommandContext.class, SuggestionsBuilder.class);
    }

    @Override
    default Collection<Pair<Producer<?>, SuggestionProvider<S>>> delegates(ArgumentContext ctx) {
        return List.of(new SimplePair<>(this.delegate(ctx), this.suggests() ? this : null));
    }

    @Override
    default CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return new CompletableFuture<>();
    }

    @Override @SuppressWarnings("unchecked")
    default T contextualize(CommandContext<S> context, Object[] subject) {
        return this.map(context, (I) subject[0]);
    }
}
