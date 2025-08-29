package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;

public interface TransformAssembler<S, I, T> extends ComposedAssembler<S, T>, SuggestionProvider<S> {

    Assembler<? super S, ? extends I> delegate();

    T compose(ExecutionContext<? extends S> ctx, I object);

    default boolean suggests() {
        return Utils.hasMethod(this.getClass(), "getSuggestions", CommandContext.class, SuggestionsBuilder.class);
    }

    @Override
    default void composedOf(AssemblerHook<S> hook) {
        var h = hook.hook("dep", this.delegate());
        if (this.suggests()) { h.suggests(this); }
    }

    @Override
    default CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return null;
    }

    @Override @SuppressWarnings("unchecked")
    default T contextualize(ExecutionContext<? extends S> ctx) {
        return this.compose(ctx, (I) ctx.component("dep"));
    }
}
