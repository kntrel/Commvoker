package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.binding.Suggester;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;

public interface BiComposedAssembler<S, A, B, T> extends ComposedAssembler<S, T> {

    Assembler<? super S, ? extends A> firstDelegate();
    Assembler<? super S, ? extends B> secondDelegate();

    T compose(ExecutionContext<? extends S> ctx, A first, B second);

    default CompletableFuture<Suggestions> firstSuggest(ExecutionContext<? extends S> ctx, SuggestionsBuilder suggestionsBuilder) {
        return new CompletableFuture<>();
    }
    default CompletableFuture<Suggestions> secondSuggest(ExecutionContext<? extends S> ctx, A first, SuggestionsBuilder suggestionsBuilder) {
        return new CompletableFuture<>();
    }

    default boolean firstSuggests() {
        return Utils.hasMethod(this.getClass(), "firstSuggest", ExecutionContext.class, SuggestionsBuilder.class);
    }
    default boolean secondSuggests() {
        return Utils.hasMethod(this.getClass(), "secondSuggest", ExecutionContext.class, Object.class, SuggestionsBuilder.class);
    }

    @Override
    default void composedOf(AssemblerHook<S> hook) {
        Suggester<S> firstProvider = this::firstSuggest,
                     secondProvider = (ctx, b) -> {
                            if (ctx.hasComponent("dep1")) {
                                A first = (A) ctx.component("dep1");
                                return this.secondSuggest(ctx, first, b);
                            }
                            return b.buildFuture();
                     };

        var h = hook.hook("dep1", this.firstDelegate());
        if (this.firstSuggests()) { h.suggests(firstProvider); }
        h = hook.hook("dep2", this.secondDelegate());
        if (this.secondSuggests()) { h.suggests(secondProvider); }
    }

    @Override @SuppressWarnings("unchecked")
    default T contextualize(ExecutionContext<? extends S> ctx) {
        return this.compose(ctx, (A) ctx.component("dep1"), (B) ctx.component("dep2"));
    }
}
