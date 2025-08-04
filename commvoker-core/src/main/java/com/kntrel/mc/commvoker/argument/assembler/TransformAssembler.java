package com.kntrel.mc.commvoker.argument.assembler;

import com.kntrel.util.tuple.Pair;
import com.kntrel.util.tuple.impl.SimplePair;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TransformAssembler<S, I, T> extends ComposedAssembler<S, T>, SuggestionProvider<S> {

    Assembler<? super S, ? extends I> delegate();

    T contextualize(CommandContext<? extends S> ctx, I object);

    default boolean suggests() {
        return Utils.hasMethod(this.getClass(), "getSuggestions", CommandContext.class, SuggestionsBuilder.class);
    }

    @Override
    default Collection<Pair<Assembler<? super S, ?>, SuggestionProvider<? super S>>> delegates() {
        return List.of(new SimplePair<>(this.delegate(), this.suggests() ? this : null));
    }

    @Override
    default CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return null;
    }

    @Override @SuppressWarnings("unchecked")
    default T contextualize(CommandContext<? extends S> ctx, Object[] objects) {
        return this.contextualize(ctx, (I) objects[0]);
    }
}
