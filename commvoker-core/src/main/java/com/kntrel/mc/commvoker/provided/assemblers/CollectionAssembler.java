package com.kntrel.mc.commvoker.provided.assemblers;

import com.kntrel.mc.commvoker.assembler.ArgumentTypeAssembler;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.ComposedAssembler;
import com.kntrel.util.tuple.Pair;
import com.kntrel.util.tuple.impl.SimplePair;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class CollectionAssembler<T, C extends Collection<T>> implements ComposedAssembler<Object, C> {

    //FACTORY
    public static <T> CollectionAssembler<T, List<T>> listOf(Assembler<?, T> delegate) {
        return new CollectionAssembler<>(delegate, ArrayList::new);
    }
    public static <T> CollectionAssembler<T, Set<T>> setOf(Assembler<?, T> delegate) {
        return new CollectionAssembler<>(delegate, HashSet::new);
    }
    public static <T, C extends Collection<T>> CollectionAssembler<T, C> collectionOf(Assembler<?, T> delegate, Supplier<C> supplier) {
        return new CollectionAssembler<>(delegate, supplier);
    }



    //FIELDS
    private final Assembler<?, T> delegate_;
    private final Supplier<C> producer_;


    //CONSTRUCTOR
    private CollectionAssembler(Assembler<?, T> delegate, Supplier<C> producer) {
        this.delegate_ = delegate;
        this.producer_ = producer;
    }


    //IMPLEMENTATION
    @Override
    public List<Pair<Assembler<? super Object, ?>, SuggestionProvider<? super Object>>> delegates() {
        return List.of(new SimplePair<>(new CollectionArgumentType<>(this.delegate_), new CollectionSuggestionProvider<>()));
    }
    @Override @SuppressWarnings("unchecked")
    public C compose(CommandContext<?> ctx, Object[] objects) {
        Collection<Object[]> rawElms = (Collection<Object[]>) objects[0];

        C out = producer_.get();
        for (Object[] leaves : rawElms) {
            T value;
            if (this.delegate_ instanceof ComposedAssembler<?, ?> comp) {
                // Safe: leaves belong to delegate_'s tree; ctx type is erased at runtime
                value = ((ComposedAssembler<Object, T>) comp).contextualize(ctx, leaves);
            } else {
                // EndAssembler case: a single leaf is the value
                if (leaves.length != 1) {
                    throw new IllegalStateException("Element assembler produced " + leaves.length + " leaves; expected 1");
                }
                value = (T) leaves[0];
            }
            out.add(value);
        }
        return out;
    }



    private static class CollectionSuggestionProvider<S> implements SuggestionProvider<S> {

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            return null;
        }
    }

    private static class CollectionArgumentType<T> implements ArgumentTypeAssembler<Collection<Object[]>> {

        private final Assembler<?, T> delegate_;

        CollectionArgumentType(Assembler<?, T> delegate) {
            this.delegate_ = delegate;
        }

        @Override
        public Collection<Object[]> parse(StringReader reader) throws CommandSyntaxException {
            reader.skipWhitespace();
            reader.expect('[');
            reader.skipWhitespace();

            List<Object[]> out = new ArrayList<>();

            // Empty list: "[]"
            if (reader.canRead() && reader.peek() == ']') {
                reader.read();
                return out;
            }

            // First element
            out.add(this.delegate_.parseRaw(reader));

            // (, elem)* then ']'
            while (true) {
                reader.skipWhitespace();
                if (!reader.canRead()) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                            .readerExpectedSymbol().createWithContext(reader, "]");
                }
                char c = reader.peek();
                if (c == ',') {
                    reader.read();
                    reader.skipWhitespace();
                    out.add(delegate_.parseRaw(reader));
                } else if (c == ']') {
                    reader.read();
                    break;
                } else {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                            .readerExpectedSymbol().createWithContext(reader, "',' or ']'");
                }
            }
            return out;
        }
    }
}