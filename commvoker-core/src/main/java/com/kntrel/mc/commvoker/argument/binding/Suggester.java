package com.kntrel.mc.commvoker.argument.binding;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;

public interface Suggester<S> {

    CompletableFuture<Suggestions> suggest(ExecutionContext<? extends S> ctx, SuggestionsBuilder builder);
}
