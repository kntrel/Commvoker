package com.kntrel.mc.commvoker.builtin.argumentType;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class CollectionArgumentType<T, C extends Collection<T>> implements ArgumentType<C> {


    public static <T, C extends Collection<T>> CollectionArgumentType<T, C> collectionOf(ArgumentType<T> wrapped, Supplier<C> supplier, int size) {
        return new CollectionArgumentType<>(supplier, wrapped, size);
    }
    public static <T, C extends Collection<T>> CollectionArgumentType<T, C> collectionOf(ArgumentType<T> wrapped, Supplier<C> supplier) {
        return collectionOf(wrapped, supplier, -1);
    }
    public static <T> CollectionArgumentType<T, Collection<T>> collectionOf(ArgumentType<T> wrapped, int size) {
        return collectionOf(wrapped, ArrayList::new, -1);
    }
    public static <T> CollectionArgumentType<T, Collection<T>> collectionOf(ArgumentType<T> wrapped) {
        return collectionOf(wrapped, -1);
    }
    public static <T> CollectionArgumentType<T, List<T>> listOf(ArgumentType<T> wrapped, int size) {
        return collectionOf(wrapped, ArrayList::new, size);
    }
    public static <T> CollectionArgumentType<T, List<T>> listOf(ArgumentType<T> wrapped) {
        return listOf(wrapped, -1);
    }
    public static <T> CollectionArgumentType<T, Set<T>> setOf(ArgumentType<T> wrapped, int size) {
        return collectionOf(wrapped, HashSet::new, size);
    }
    public static <T> CollectionArgumentType<T, Set<T>> setOf(ArgumentType<T> wrapped) {
        return setOf(wrapped, -1);
    }


    private final Supplier<C> supplier_;
    private final ArgumentType<T> wrapped_;
    private final int size_;

    private CollectionArgumentType(Supplier<C> supplier, ArgumentType<T> wrapped, int size) {
        this.supplier_ = supplier;
        this.wrapped_ = wrapped;
        this.size_ = size;
    }


    @Override
    public C parse(StringReader reader) throws CommandSyntaxException {
        C out = this.supplier_.get();


        for (int i = 0; i < this.size_; i++) {
            if (!reader.canRead()) { break; }
            out.add(this.wrapped_.parse(reader));
        }
        return out;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return this.wrapped_.listSuggestions(context, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return this.wrapped_.getExamples();
    }
}
