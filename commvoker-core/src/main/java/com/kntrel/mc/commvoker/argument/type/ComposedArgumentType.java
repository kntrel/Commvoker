package com.kntrel.mc.commvoker.argument.type;

import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.mc.commvoker.argument.assembler.Contextualizer;
import com.kntrel.util.tuple.Pair;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;

public non-sealed interface ComposedArgumentType<S, T> extends Contextualizer<S, Object[], T>, Producer<T> {

    Collection<Pair<Producer<?>, SuggestionProvider<S>>> delegates(ArgumentContext ctx);
}
