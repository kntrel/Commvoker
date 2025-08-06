package com.kntrel.mc.commvoker.assembler;

import com.kntrel.util.tuple.Pair;
import com.kntrel.util.tuple.impl.SimplePair;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TriComposedAssembler<S, A, B, C, T> extends ComposedAssembler<S, T> {

    Assembler<? super S, ? extends A> firstDelegate();
    Assembler<? super S, ? extends B> secondDelegate();
    Assembler<? super S, ? extends C> thirdDelegate();

    T compose(CommandContext<? extends S> ctx, A first, B second, C third);

    default CompletableFuture<Suggestions> getFirstSuggestions(CommandContext<S> ctx, SuggestionsBuilder suggestionsBuilder) {
        return new CompletableFuture<>();
    }
    default CompletableFuture<Suggestions> getSecondSuggestions(CommandContext<S> ctx, SuggestionsBuilder suggestionsBuilder) {
        return new CompletableFuture<>();
    }
    default CompletableFuture<Suggestions> getThirdSuggestions(CommandContext<S> ctx, SuggestionsBuilder suggestionsBuilder) {
        return new CompletableFuture<>();
    }

    default boolean firstSuggests() {
        return Utils.hasMethod(this.getClass(), "getFirstSuggestions", CommandContext.class, SuggestionsBuilder.class);
    }
    default boolean secondSuggests() {
        return Utils.hasMethod(this.getClass(), "getSecondSuggestions", CommandContext.class, SuggestionsBuilder.class);
    }
    default boolean thirdSuggests() {
        return Utils.hasMethod(this.getClass(), "getThirdSuggestions", CommandContext.class, SuggestionsBuilder.class);
    }

    @Override
    default List<Pair<Assembler<? super S, ?>, SuggestionProvider<? super S>>> delegates() {
        SuggestionProvider<? super S> firstProvider = this::getFirstSuggestions,
                                      secondProvider = this::getSecondSuggestions,
                                      thirdProvider = this::getThirdSuggestions;
        return List.of(
                new SimplePair<>(this.firstDelegate(), this.firstSuggests() ? firstProvider : null),
                new SimplePair<>(this.secondDelegate(), this.secondSuggests() ? secondProvider : null),
                new SimplePair<>(this.thirdDelegate(), this.thirdSuggests() ? thirdProvider : null)
        );
    }

    @Override @SuppressWarnings("unchecked")
    default T compose(CommandContext<? extends S> ctx, Object[] objects) {
        return this.compose(ctx, (A) objects[0], (B) objects[1], (C) objects[2]);
    }

}
