package com.kntrel.mc.commvoker.argument;

import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import java.util.*;

public sealed interface Component<S> extends Comparable<Component<S>> {

    //FACTORY
    static <S> Leaf<S> fromNodes(String key, Object val, Collection<ParsedCommandNode<S>> node) {
        return new Leaf<>(key, val, node);
    }
    static <S> Composite<S> of(String key, Object val, Collection<? extends Component<S>> components) {
        return new Composite<>(key, val, components);
    }
    @SafeVarargs
    static <S> Composite<S> of(String key, Object val, Component<S>... components) {
        return new Composite<>(key, val, List.of(components));
    }


    //CONTRACT
    List<ParsedCommandNode<S>> commandNodes();
    String key();
    Object value();
    StringRange range();


    //DEFAULTS
    @Override default int compareTo(Component<S> o) {
        return Integer.compare(this.range().getStart(), o.range().getStart());
    }


    //IMPLEMENTATIONS
    final class Leaf<S> implements Component<S> {
        private final String key_;
        private final Object value_;
        private final List<ParsedCommandNode<S>> nodes_;

        public Leaf(String key, Object value, Collection<ParsedCommandNode<S>> nodes) {
            this.key_ = key;
            this.value_ = value;
            this.nodes_ = nodes.stream()
                    .sorted(Comparator.comparingInt(n -> n.getRange().getStart()))
                    .toList();
        }

        @Override public String key() { return this.key_; }
        @Override public Object value() { return this.value_;}
        @Override public List<ParsedCommandNode<S>> commandNodes() { return this.nodes_; }
        @Override public StringRange range() {
            if (this.nodes_.isEmpty()) {
                return StringRange.at(0);
            }
            return StringRange.between(
                    this.nodes_.getFirst().getRange().getStart(),
                    this.nodes_.getLast().getRange().getEnd()
            );
        }
    }

    final class Composite<S> implements Component<S> {
        private final List<? extends Component<S>> components_;
        private final String key_;
        private final Object value_;

        public Composite(String key, Object value, Collection<? extends Component<S>> components) {
            this.key_ = key;
            this.value_ = value;
            this.components_ = components.stream()
                    .sorted(Comparator.comparingInt(c -> c.range().getStart()))
                    .toList();
        }

        public List<? extends Component<S>> components() { return this.components_; }
        @Override public String key() { return this.key_; }
        @Override public Object value() { return this.value_;}
        @Override public List<ParsedCommandNode<S>> commandNodes() {
            List<ParsedCommandNode<S>> nodes = new ArrayList<>();

            Deque<Component<S>> stack = new ArrayDeque<>();
            stack.add(this);
            while (!stack.isEmpty()) {
                Component<S> component = stack.pop();
                if (component instanceof Composite<S> composite) {
                    stack.addAll(composite.components().reversed());
                } else if (component instanceof Leaf<S> leaf) {
                    nodes.addAll(leaf.commandNodes());
                }
            }
            return nodes;
        }
        @Override public StringRange range() {
            if (this.components_.isEmpty()) {
                return StringRange.at(0);
            }
            return StringRange.between(
                    this.components_.getFirst().range().getStart(),
                    this.components_.getLast().range().getEnd()
            );
        }
    }
}
