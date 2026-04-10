package com.kntrel.mc.commvoker.argument;

import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import java.util.*;

/**
 * Internal API. Public for framework wiring, but not part of the supported external API.
 */
public sealed interface Component<S> extends Comparable<Component<S>> {

    //FACTORY
    static <S> Leaf<S> fromNodes(String key, Object val, Collection<ParsedCommandNode<? super S>> node) {
        return new Leaf<>(key, val, node);
    }
    static <S> Composite<S> of(String key, Object val, Collection<? extends Component<? super S>> components) {
        return new Composite<>(key, val, components);
    }
    @SafeVarargs
    static <S> Composite<S> of(String key, Object val, Component<? super S>... components) {
        return new Composite<S>(key, val, List.of(components));
    }


    //CONTRACT
    List<ParsedCommandNode<? super S>> commandNodes();
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
        private final List<ParsedCommandNode<? super S>> nodes_;

        public Leaf(String key, Object value, Collection<ParsedCommandNode<? super S>> nodes) {
            this.key_ = key;
            this.value_ = value;
            this.nodes_ = nodes.stream()
                    .sorted(Comparator.comparingInt(n -> n.getRange().getStart()))
                    .toList();
        }

        @Override public String key() { return this.key_; }
        @Override public Object value() { return this.value_;}
        @Override public List<ParsedCommandNode<? super S>> commandNodes() { return this.nodes_; }
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
        private final List<? extends Component<? super S>> components_;
        private final String key_;
        private final Object value_;

        public Composite(String key, Object value, Collection<? extends Component<? super S>> components) {
            this.key_ = key;
            this.value_ = value;
            this.components_ = components.stream()
                    .sorted(Comparator.comparingInt(c -> c.range().getStart()))
                    .toList();
        }

        public List<? extends Component<? super S>> components() { return this.components_; }
        @Override public String key() { return this.key_; }
        @Override public Object value() { return this.value_;}
        @Override public List<ParsedCommandNode<? super S>> commandNodes() {
            List<ParsedCommandNode<? super S>> nodes = new ArrayList<>();

            Deque<Component<? super S>> stack = new ArrayDeque<>();
            stack.add(this);
            while (!stack.isEmpty()) {
                Component<? super S> component = stack.pop();
                if (component instanceof Composite<? super S> composite) {
                    stack.addAll(composite.components().reversed());
                } else if (component instanceof Leaf<? super S> leaf) {
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
