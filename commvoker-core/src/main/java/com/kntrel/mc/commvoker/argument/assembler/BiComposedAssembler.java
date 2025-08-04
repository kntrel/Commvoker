package com.kntrel.mc.commvoker.argument.assembler;

import com.kntrel.util.tuple.Pair;
import com.kntrel.util.tuple.impl.SimplePair;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BiComposedAssembler<S, A, B, T> extends ComposedAssembler<S, T> {

    Assembler<? super S, ? extends A> firstDelegate();
    Assembler<? super S, ? extends B> secondDelegate();

    T contextualize(CommandContext<? extends S> ctx, A first, B second);

    default CompletableFuture<Suggestions> getFirstSuggestions(CommandContext<S> ctx, SuggestionsBuilder suggestionsBuilder) {
        return new CompletableFuture<>();
    }
    default CompletableFuture<Suggestions> getSecondSuggestions(CommandContext<S> ctx, SuggestionsBuilder suggestionsBuilder) {
        return new CompletableFuture<>();
    }

    default boolean firstSuggests() {
        return Utils.hasMethod(this.getClass(), "getFirstSuggestions", CommandContext.class, SuggestionsBuilder.class);
    }
    default boolean secondSuggests() {
        return Utils.hasMethod(this.getClass(), "getSecondSuggestions", CommandContext.class, SuggestionsBuilder.class);
    }

    @Override
    default Collection<Pair<Assembler<? super S, ?>, SuggestionProvider<? super S>>> delegates() {
        SuggestionProvider<? super S> firstProvider = this::getFirstSuggestions,
                                      secondProvider = this::getSecondSuggestions;
        return List.of(
                new SimplePair<>(this.firstDelegate(), this.firstSuggests() ? firstProvider : null),
                new SimplePair<>(this.secondDelegate(), this.secondSuggests() ? secondProvider : null)
        );
    }

    @Override @SuppressWarnings("unchecked")
    default T contextualize(CommandContext<? extends S> ctx, Object[] objects) {
        return this.contextualize(ctx, (A) objects[0], (B) objects[1]);
    }

}
