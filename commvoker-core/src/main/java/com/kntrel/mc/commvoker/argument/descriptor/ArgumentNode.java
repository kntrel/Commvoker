package com.kntrel.mc.commvoker.argument.descriptor;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

public record ArgumentNode<S, T>(ArgumentType<T> argumentType, SuggestionProvider<S> suggestionProvider) {}
