package com.kntrel.mc.commvoker.argument.type;

import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.util.tuple.Pair;
import com.kntrel.util.tuple.impl.SimplePair;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BiComposedArgumentType<S, A, B, T> extends ComposedArgumentType<S, T> {

    Producer<A> firstDelegate(ArgumentContext ctx);
    Producer<B> secondDelegate(ArgumentContext ctx);

    T compose(CommandContext<S> ctx, A first, B second);

    default boolean firstSuggests() {
        return TypeUtils.hasMethod(this.getClass(), "getSuggestionsFirst", CommandContext.class, SuggestionsBuilder.class);
    }
    default boolean secondSuggests() {
        return TypeUtils.hasMethod(this.getClass(), "getSuggestionsSecond", CommandContext.class, SuggestionsBuilder.class);
    }

    default CompletableFuture<Suggestions> getSuggestionsFirst(CommandContext<S> ctx, SuggestionsBuilder suggestionsBuilder) {
        return new CompletableFuture<>();
    }
    default CompletableFuture<Suggestions> getSuggestionsSecond(CommandContext<S> ctx, SuggestionsBuilder suggestionsBuilder) {
        return new CompletableFuture<>();
    }

    @Override
    default Collection<Pair<Producer<?>, SuggestionProvider<S>>> delegates(ArgumentContext ctx) {
        SuggestionProvider<S> firstProvider = this::getSuggestionsFirst,
                              secondProvider = this::getSuggestionsSecond;
        return List.of(
                new SimplePair<>(this.firstDelegate(ctx), this.firstSuggests() ? firstProvider : null),
                new SimplePair<>(this.secondDelegate(ctx), this.secondSuggests() ? secondProvider : null)
        );
    }
    @Override @SuppressWarnings("unchecked")
    default T contextualize(CommandContext<S> context, Object[] subject) {
        return this.compose(context, (A) subject[0], (B) subject[1]);
    }
}
