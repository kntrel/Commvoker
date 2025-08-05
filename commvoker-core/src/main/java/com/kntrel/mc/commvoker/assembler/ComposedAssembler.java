package com.kntrel.mc.commvoker.assembler;

import com.kntrel.util.tuple.Pair;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;

public non-sealed interface ComposedAssembler<S, T> extends Assembler<S, T> {

    Collection<Pair<Assembler<? super S, ?>, SuggestionProvider<? super S>>> delegates();

    T contextualize(CommandContext<? extends S> ctx, Object[] objects);

    @Override
    default boolean isImplicit() {
        for (var d : this.delegates()) {
            if (!d.first().isImplicit()) { return false; }
        }
        return true;
    }
}
