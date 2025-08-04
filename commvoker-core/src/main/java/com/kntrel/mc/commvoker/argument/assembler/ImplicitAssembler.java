package com.kntrel.mc.commvoker.argument.assembler;

import com.kntrel.util.tuple.Pair;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public interface ImplicitAssembler<S, T> extends ComposedAssembler<S, T>, Function<CommandContext<? extends S>, T> {

    @Override
    default boolean isImplicit() {
        return true;
    }

    @Override
    default Collection<Pair<Assembler<? super S, ?>, SuggestionProvider<? super S>>> delegates() {
        return Collections.emptyList();
    }

    @Override
    default T contextualize(CommandContext<? extends S> ctx, Object[] objects) {
        return this.apply(ctx);
    }
}