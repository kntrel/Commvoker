package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.binding.*;
import com.mojang.brigadier.context.CommandContext;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public sealed abstract class CompiledAssembler<S, T> implements ArgumentDescriptor<S, T>, Contextualizer<S, T> {

    public static <S, T> CompiledAssembler<S, T> of(Assembler<S, T> assembler) {
        return switch (assembler) {
            case EndAssembler<S, T> end -> new Leave<>(end);
            case ComposedAssembler<S, T> comp -> new Composed<>(comp);
        };
    }


    @Override
    public CommandTemplate.Node<S> argumentTrees() {
        return this.treeGate(new HashMap<>()).root();
    }

    @Override
    public Contextualizer<S, T> contextualizer() {
        return this;
    }


    protected abstract TreeGate<S> treeGate(Map<String, AtomicInteger> argCount);

    protected abstract Assembler<S, T> assembler();

    private static final class Composed<S, T> extends CompiledAssembler<S, T> {

        private final ComposedAssembler<S, T> assembler_;
        private final LinkedHashMap<String, CompiledAssembler<? super S, ?>> children_;

        Composed(ComposedAssembler<S, T> assembler) {
            this.assembler_ = assembler;
            this.children_ = new LinkedHashMap<>();

            AssemblerHook<S> hook = new AssemblerHook<>();
            this.assembler_.composedOf(hook);
            hook.nodeMap().forEach((k, v) -> {
                CompiledAssembler<? super S, ?> child = CompiledAssembler.of(v.assembler());
                this.children_.put(k, child);
            });
        }

        @Override
        protected ComposedAssembler<S, T> assembler() {
            return this.assembler_;
        }

        @Override
        public T contextualize(CommandContext<? extends S> ctx, Components components) {
            Map<String, Object> compMap = this.children_.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().contextualize(ctx, components)
            ));
            return this.assembler_.contextualize(ctx, new Components(compMap));
        }

        @Override @SuppressWarnings({ "unchecked", "rawtypes" })
        protected TreeGate<S> treeGate(Map<String, AtomicInteger> argCount) {
            if (this.children_.isEmpty()) {
                throw new IllegalStateException("Assembler '" + this.assembler_.getClass().getSimpleName() + "' has no dependencies");
            }

            CommandTemplate.Node<S> root = null;
            Collection<CommandTemplate.Node<S>> upstream = null;
            for (CompiledAssembler<? super S, ?> child : this.children_.values()) {
                TreeGate<? super S> childTree = child.treeGate(argCount);
                CommandTemplate.Node<? super S> cRoot = childTree.root();
                if (root == null) {
                    root = (CommandTemplate.Node) cRoot;
                }
                Collection<? extends CommandTemplate.Node<? super S>> cLeaves = childTree.leaves();
                if (cLeaves.isEmpty()) {
                    throw new IllegalStateException("CommandTemplate of assembler '" + child.assembler().getClass().getSimpleName() + "' has no exit points");
                }
                if (upstream != null) for (CommandTemplate.Node<? super S> l : upstream) {
                    l.addChild((CommandTemplate.Node) cRoot);
                }
                upstream = (Collection<CommandTemplate.Node<S>>) cLeaves;
            }

            return new TreeGate<>(root, upstream);
        }
    }

    private static final class Leave<S, T> extends CompiledAssembler<S, T> {

        private final EndAssembler<S, T> assembler_;
        private final Map<String, String> argMap_;

        Leave(EndAssembler<S, T> assembler) {
            this.assembler_ = assembler;
            this.argMap_ = new HashMap<>();
        }

        @Override
        protected EndAssembler<S, T> assembler() {
            return this.assembler_;
        }

        @Override
        protected TreeGate<S> treeGate(Map<String, AtomicInteger> argCount) {
            CommandTemplate.Node<S> root = this.assembler_.argumentTemplate();
            List<CommandTemplate.Node<S>> leaves = new ArrayList<>();
            List<CommandTemplate.Forward<S>> forwards = new ArrayList<>();

            Set<String> labels = new HashSet<>();
            Deque<CommandTemplate.Node<S>> dfs = new ArrayDeque<>();
            dfs.add(root);

            while (!dfs.isEmpty()) {
                CommandTemplate.Node<S> n = dfs.pollLast();
                String label = n.label();
                if (labels.contains(label)) {
                    throw new IllegalStateException("Template has more that one node under the label '" + label + "'");
                }
                labels.add(label);

                AtomicInteger count = argCount.computeIfAbsent(label,l -> new AtomicInteger(0));
                int c = count.getAndIncrement();
                if (c > 0) { n.rename(label + c); }
                this.argMap_.put(label, n.label());

                if (n.children().isEmpty()) {
                    leaves.add(n);
                } else for (CommandTemplate<S> child : n.children()) switch (child) {
                    case CommandTemplate.Node<S> ch -> dfs.addLast(ch);
                    case CommandTemplate.Forward<S> fw -> forwards.add(fw);
                }
            }

            forwards.forEach(fw -> {
                String label = this.argMap_.get(fw.forwardsTo());
                if (label != null) { fw.reforward(label); }
            });

            return new TreeGate<>(root, leaves);
        }

        @Override
        public T contextualize(CommandContext<? extends S> ctx, Components components) {
            Map<String, Object> compMap = this.argMap_.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> components.get(e.getValue())
            ));
            return this.assembler_.contextualize(ctx, new Components(compMap));
        }
    }

    protected record TreeGate<S>(CommandTemplate.Node<S> root, Collection<CommandTemplate.Node<S>> leaves) {}

}
