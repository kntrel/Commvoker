package com.kntrel.mc.commvoker.provided.assemblers;


import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.argument.binding.Components;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.CompiledAssembler;
import com.kntrel.mc.commvoker.assembler.EndAssembler;
import com.kntrel.util.tuple.Pair;
import com.mojang.brigadier.context.CommandContext;
import java.util.*;
import java.util.function.Function;

public class CollectionAssembler<S, T, C extends Collection<T>> implements EndAssembler<S, C> {

    //FACTORY
    public static <S, T, C extends Collection<T>> CollectionAssembler<S, T, C> collectionOf(Assembler<S, T> element, int min, int max, Function<Collection<T>, C> composer) {
        return new CollectionAssembler<>(min, max, element, composer);
    }
    public static <S, T, C extends Collection<T>> CollectionAssembler<S, T, C> collectionOf(Assembler<S, T> element, int max, Function<Collection<T>, C> composer) {
        return collectionOf(element, 0, max, composer);
    }
    public static <S, T> CollectionAssembler<S, T, List<T>> listOf(Assembler<S, T> element, int min, int max) {
        return collectionOf(element, min, max, List::copyOf);
    }
    public static <S, T> CollectionAssembler<S, T, List<T>> listOf(Assembler<S, T> element, int max) {
        return listOf(element, 0, max);
    }
    public static <S, T> CollectionAssembler<S, T, List<T>> listOf(Assembler<S, T> element) {
        return listOf(element, 0, 8);
    }
    public static <S, T> CollectionAssembler<S, T, Set<T>> setOf(Assembler<S, T> element, int min, int max) {
        return collectionOf(element, min, max, Set::copyOf);
    }
    public static <S, T> CollectionAssembler<S, T, Set<T>> setOf(Assembler<S, T> element, int max) {
        return setOf(element, 0, max);
    }
    public static <S, T> CollectionAssembler<S, T, Set<T>> setOf(Assembler<S, T> element) {
        return setOf(element, 0, 8);
    }


    //ASSETS
    private record Delegate<S>(CompiledAssembler.TreeGate<S> tree, Map<String, String> namesMap) {}


    //FIELDS
    private final int min_, max_;
    private final Assembler<S, T> delegate_;
    private final Delegate<S>[] delegates_;
    private final Function<Collection<T>, C> composer_;


    //CONSTRUCTOR
    private CollectionAssembler(int min, int max, Assembler<S, T> delegate, Function<Collection<T>, C> composer) {
        if (min < 0) {
            throw new IllegalArgumentException("A collection's min length cannot be lower than 0");
        }
        if (max < min) {
            throw new IllegalArgumentException("A collection's max length must be bigger or equals its min length");
        }
        if (min < 1 && min == max) {
            throw new IllegalArgumentException("A zero-only length collection is not allowed");
        }

        this.min_ = min;
        this.max_ = max;
        this.delegate_ = delegate;
        this.composer_ = composer;

        this.delegates_ = new Delegate[this.max_];
        CommandTemplate.Node<S> tree = CompiledAssembler.of(this.delegate_).argumentTrees();
        for (int i = 0; i < this.max_; i++) {
            CommandTemplate.Node<S> clone = tree.clone();
            final int index = i;
            var cloneResult = renameTree(clone, s -> s + index);
            CompiledAssembler.TreeGate<S> treeGate = new CompiledAssembler.TreeGate<>(clone, cloneResult.first());
            this.delegates_[i] = new Delegate<>(treeGate, cloneResult.second());
        }
    }


    @Override
    public CommandTemplate.Node<S> argumentTemplate() {
        CompiledAssembler.TreeGate<S> rootTree = this.delegates_[0].tree();
        CommandTemplate.Node<S> root = rootTree.root();
        if (this.max_ < 2) { return root; }

        CompiledAssembler.TreeGate<S> lastTree = this.delegates_[this.max_ - 1].tree();
        CommandTemplate.Node<S> last = lastTree.root();
        CommandTemplate.Node<S> andNode = CommandTemplate.<S>beginLiteral("and").end();
        andNode.addChild(last);

        Collection<CommandTemplate.Node<S>> upstream = rootTree.leaves();
        for (int i = 1; i < this.max_; i++) {
            if (i == (this.max_ - 1)) {
                upstream.forEach(n -> n.addChild(andNode));
                break;
            }

            CompiledAssembler.TreeGate<S> tree = this.delegates_[i].tree();
            CommandTemplate.Node<S> next = tree.root();

            for (CommandTemplate.Node<S> n : upstream) {
                n.addChild(next);
                if (i >= this.min_) {
                    n.addChild(CommandTemplate.<S>beginLiteral("and").then(last.label()).end());
                }
            }

            upstream = tree.leaves();
        }

        return root;
    }

    @Override
    public C contextualize(CommandContext<? extends S> context, Components components) {
        List<T> list = new ArrayList<>(this.max_);
        outer : for (int i = 0; i < this.max_; i++) {
            Map<String, Object> compMap = new HashMap<>();
            for (Map.Entry<String, String> entry : this.delegates_[i].namesMap().entrySet()) {
                Object o = components.get(entry.getValue());
                if (o == null) { continue outer; }
                compMap.put(entry.getKey(), o);
            }

            T elm = this.delegate_.contextualize(context, new Components(compMap));
            list.add(elm);
        }

        return this.composer_.apply(list);
    }


    private static <S> Pair<Collection<CommandTemplate.Node<S>>, Map<String, String>> renameTree(CommandTemplate<S> root, Function<String, String> renamer) {
        Deque<CommandTemplate<S>> stack = new ArrayDeque<>();
        stack.add(root);
        List<CommandTemplate.Forward<S>> forwards = new ArrayList<>();
        Map<String, String> namesMap = new HashMap<>();
        List<CommandTemplate.Node<S>> leaves = new ArrayList<>();

        while (!stack.isEmpty()) switch (stack.pollLast()) {
            case CommandTemplate.Node<S> n -> {
                if (n instanceof CommandTemplate.Argument<?>) {
                    String old = n.label();
                    String rename = renamer.apply(old);
                    n.rename(rename);
                    namesMap.put(old, rename);
                }
                if (n.children().isEmpty()) {
                    leaves.add(n);
                } else for (CommandTemplate<S> c : n.children()) { stack.addLast(c); }
            }
            case CommandTemplate.Forward<S> f -> forwards.add(f);
        }

        forwards.forEach(f -> f.reforward(namesMap.get(f.forwardsTo())));

        return Pair.of(leaves, namesMap);
    }
}