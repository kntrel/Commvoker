package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.argument.binding.*;
import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public sealed abstract class CompiledAssembler<S, T> implements ArgumentDescriptor<S, T>, Contextualizer<S, T> {

    public static <S, T> CompiledAssembler<S, T> of(Assembler<S, T> assembler) {
        return switch (assembler) {
            case EndAssembler<S, T> end    -> new Leave<>(end);
            case ComposedAssembler<S, T> c -> new Composed<>(c);
        };
    }

    @Override
    public CommandTemplate<S> template() {
        return this.template(new HashMap<>());
    }

    @Override
    public Contextualizer<S, T> contextualizer() {
        return this;
    }

    protected abstract CommandTemplate<S> template(Map<String, AtomicInteger> argCount);

    protected abstract Assembler<S, T> assembler();


    /* ---------------------------------------------------- COMPOSED ---------------------------------------------------- */

    private static final class Composed<S, T> extends CompiledAssembler<S, T> {

        private final ComposedAssembler<S, T> assembler_;
        private final LinkedHashMap<String, CompiledAssembler<? super S, ?>> children_;
        private final Map<String, SuggestionProvider<? extends S>> suggesters_;

        Composed(ComposedAssembler<S, T> assembler) {
            this.assembler_  = assembler;
            this.children_   = new LinkedHashMap<>();
            this.suggesters_ = new HashMap<>();

            AssemblerHook<S> hook = new AssemblerHook<>();
            this.assembler_.composedOf(hook);
            hook.nodeMap().forEach((k, v) -> {
                CompiledAssembler<? super S, ?> child = CompiledAssembler.of(v.assembler());
                this.children_.put(k, child);
                SuggestionProvider<? extends S> sug = v.suggester();
                if (sug != null) { this.suggesters_.put(k, sug); }
            });
        }

        @Override
        protected ComposedAssembler<S, T> assembler() {
            return this.assembler_;
        }

        @Override
        public T contextualize(ExecutionContext<? extends S> ctx) {
            Map<String, Object> compMap = this.children_.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().contextualize(ctx)
            ));
            return this.assembler_.contextualize(ExecutionContext.copyOf(ctx, compMap));
        }

        @Override @SuppressWarnings({ "unchecked", "rawtypes" })
        protected CommandTemplate<S> template(Map<String, AtomicInteger> argCount) {
            if (this.children_.isEmpty()) {
                throw new IllegalStateException("Assembler '" + this.assembler_.getClass().getSimpleName() + "' has no dependencies");
            }

            List<CommandTemplate.Node<S>> roots = null;
            List<CommandTemplate.Node<S>> upstream = null;

            for (var e : this.children_.entrySet()) {
                CompiledAssembler<? super S, ?> child = e.getValue();
                CommandTemplate<? super S> ct = child.template(argCount);

                // Apply suggester to entry roots that are arguments
                SuggestionProvider<? extends S> sug = this.suggesters_.get(e.getKey());
                if (sug != null) {
                    for (CommandTemplate.Node<? super S> r : ct.trees()) {
                        switch (r) {
                            case CommandTemplate.Argument<? super S> arg -> arg.setSuggestionProvider(sug);
                            default -> {}
                        }
                    }
                }

                if (roots == null) {
                    roots = (List) ct.trees();                    // first child's roots become global roots
                    upstream = (List) ct.exitPoints();            // exits are where we connect the next child
                    if (upstream == null || upstream.isEmpty()) {
                        throw new IllegalStateException("CommandTemplate of assembler '" + child.assembler().getClass().getSimpleName() + "' has no exit points");
                    }
                    continue;
                }

                // wire previous exits -> current roots
                for (CommandTemplate.Node<S> up : upstream) {
                    for (CommandTemplate.Node<S> r : (List<CommandTemplate.Node<S>>) (List) ct.trees()) {
                        up.addChild(r);
                    }
                }
                upstream = (List) ct.exitPoints();
                if (upstream == null || upstream.isEmpty()) {
                    throw new IllegalStateException("CommandTemplate of assembler '" + child.assembler().getClass().getSimpleName() + "' has no exit points");
                }
            }

            if (roots == null) {
                throw new IllegalStateException("No roots constructed for '" + this.assembler_.getClass().getSimpleName() + "'");
            }
            return CommandTemplate.split(roots.toArray(CommandTemplate.Node[]::new));
        }
    }


    /* ------------------------------------------------------ LEAF ------------------------------------------------------ */

    private static final class Leave<S, T> extends CompiledAssembler<S, T> {

        private final EndAssembler<S, T> assembler_;
        private final Map<String, String> argMap_;

        Leave(EndAssembler<S, T> assembler) {
            this.assembler_ = assembler;
            this.argMap_    = new HashMap<>();
        }

        @Override
        protected EndAssembler<S, T> assembler() {
            return this.assembler_;
        }

        @Override
        protected CommandTemplate<S> template(Map<String, AtomicInteger> argCount) {
            CommandTemplate<S> tpl = this.assembler_.argumentTemplate();
            CommandTemplate<S> root = tpl.clone(); // work on a fresh clone

            // Rename only Argument nodes for global uniqueness; track mapping for contextualize()
            Set<String> seenLabels = new HashSet<>();
            List<CommandTemplate.Forward<S>> forwards = new ArrayList<>();

            Deque<CommandTemplate.Node<S>> dfs = new ArrayDeque<>(root.trees());
            while (!dfs.isEmpty()) {
                CommandTemplate.Node<S> n = dfs.pollLast();

                switch (n) {
                    case CommandTemplate.Argument<S> arg -> {
                        String label = n.label();

                        // Ensure per-template uniqueness of argument labels (keeps forwards unambiguous here)
                        if (!seenLabels.add(label)) { continue; }

                        AtomicInteger count = argCount.computeIfAbsent(label, l -> new AtomicInteger(0));
                        int c = count.getAndIncrement();
                        if (c > 0) { n.rename(label + c); }         // suffix only arguments
                        this.argMap_.put(label, n.label());
                    }
                    default -> {}
                }

                for (CommandTemplate.Element<S> child : n.children()) switch (child) {
                    case CommandTemplate.Node<S> ch -> dfs.addLast(ch);
                    case CommandTemplate.Forward<S> fw -> forwards.add(fw);
                    default -> {}
                }
            }

            // Patch forwards that pointed to renamed argument labels (occurrence stays the same)
            forwards.forEach(fw -> {
                String mapped = this.argMap_.get(fw.forwardsTo());
                if (mapped != null) { fw.reforward(mapped); }
            });

            return root;
        }

        @Override
        public T contextualize(ExecutionContext<? extends S> ctx) {
            Map<String, Object> compMap = this.argMap_.entrySet().stream()
                    .filter(e -> ctx.hasComponent(e.getValue()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> ctx.component(e.getValue())
                    ));
            return this.assembler_.contextualize(ExecutionContext.copyOf(ctx, compMap));
        }
    }
}