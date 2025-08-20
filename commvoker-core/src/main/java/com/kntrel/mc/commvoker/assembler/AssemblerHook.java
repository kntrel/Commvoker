package com.kntrel.mc.commvoker.assembler;

import com.kntrel.util.tuple.Pair;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AssemblerHook<S> {

    public record Node<S>(Assembler<? super S, ?> assembler, SuggestionProvider<S> suggester)
    implements Pair<Assembler<? super S, ?>, SuggestionProvider<S>> {

        @Override
        public Assembler<? super S, ?> first() {
            return this.assembler();
        }

        @Override
        public SuggestionProvider<S> second() {
            return this.suggester();
        }
    }
    public static class Handle<S> {

        private final Assembler<? super S, ?> assembler_;
        private SuggestionProvider<S> suggester_;

        private Handle(Assembler<? super S, ?> assembler) { this.assembler_ = assembler; }
        public void suggests(SuggestionProvider<S> suggester) { this.suggester_ = suggester; }
        private Node<S> node() { return new Node<>(this.assembler_, this.suggester_); }

    }


    //FIELDS
    private final LinkedHashMap<String, Handle<S>> handleMap_;


    public AssemblerHook() {
        this.handleMap_ = new LinkedHashMap<>();
    }


    //UTILITY
    public Handle<S> hook(String key, Assembler<? super S, ?> assembler) {
        if (this.handleMap_.containsKey(key)) {
            throw new IllegalStateException("Handle '" + key + "' has already been defined");
        }
        Handle<S> handle = new Handle<>(assembler);
        this.handleMap_.put(key, handle);
        return handle;
    }

    LinkedHashMap<String, Node<S>> nodeMap() {
        LinkedHashMap<String, Node<S>> out = new LinkedHashMap<>();
        handleMap_.forEach((k, v) -> out.put(k, v.node()));
        return out;
    }


}
