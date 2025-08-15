package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.binding.Components;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
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
    default void composedOf(AssemblerHook<S> hook) {
        SuggestionProvider<S> firstProvider = this::getFirstSuggestions,
                              secondProvider = this::getSecondSuggestions,
                              thirdProvider = this::getThirdSuggestions;

        var h = hook.hook("dep1", this.firstDelegate());
        if (this.firstSuggests()) { h.suggests(firstProvider); }
        h = hook.hook("dep2", this.secondDelegate());
        if (this.secondSuggests()) { h.suggests(secondProvider); }
        h = hook.hook("dep3", this.thirdDelegate());
        if (this.thirdSuggests()) { h.suggests(thirdProvider); }
    }

    @Override @SuppressWarnings("unchecked")
    default T compose(CommandContext<? extends S> ctx, Components components) {
        return this.compose(ctx, (A) components.get("dep1"), (B) components.get("dep2"), (C) components.get("dep3"));
    }

}
