package com.kntrel.mc.commvoker.assembler;

import com.kntrel.util.tuple.Pair;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public interface ImplicitAssembler<S, T> extends ComposedAssembler<S, T>, Function<CommandContext<? extends S>, T> {

    @Override
    default boolean isImplicit() {
        return true;
    }

    @Override
    default List<Pair<Assembler<? super S, ?>, SuggestionProvider<? super S>>> delegates() {
        return Collections.emptyList();
    }

    @Override
    default T compose(CommandContext<? extends S> ctx, Object[] objects) {
        return this.apply(ctx);
    }
}