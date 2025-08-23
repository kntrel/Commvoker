package com.kntrel.mc.commvoker.provided.assemblers;


import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.argument.binding.Components;
import com.kntrel.mc.commvoker.argument.binding.Contextualizer;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.CompiledAssembler;
import com.kntrel.mc.commvoker.assembler.EndAssembler;
import com.mojang.brigadier.context.CommandContext;
import java.util.*;
import java.util.function.Function;

public class CollectionAssembler<S, T, C extends Collection<T>> implements EndAssembler<S, C> {

    //FACTORY
    public static <S, T, C extends Collection<T>> CollectionAssembler<S, T, C> collectionOf(Assembler<S, T> element, int min, int max, Function<Collection<T>, C> composer) {
        return new CollectionAssembler<>(min, max, false, element, composer);
    }
    public static <S, T, C extends Collection<T>> CollectionAssembler<S, T, C> relaxedCollectionOf(Assembler<S, T> element, int min, int max, Function<Collection<T>, C> composer) {
        return new CollectionAssembler<>(min, max, true, element, composer);
    }
    public static <S, T, C extends Collection<T>> CollectionAssembler<S, T, C> collectionOf(Assembler<S, T> element, int max, Function<Collection<T>, C> composer) {
        return collectionOf(element, 1, max, composer);
    }
    public static <S, T, C extends Collection<T>> CollectionAssembler<S, T, C> relaxedCollectionOf(Assembler<S, T> element, int max, Function<Collection<T>, C> composer) {
        return relaxedCollectionOf(element, 1, max, composer);
    }
    public static <S, T> CollectionAssembler<S, T, List<T>> listOf(Assembler<S, T> element, int min, int max) {
        return collectionOf(element, min, max, List::copyOf);
    }
    public static <S, T> CollectionAssembler<S, T, List<T>> relaxedListOf(Assembler<S, T> element, int min, int max) {
        return relaxedCollectionOf(element, min, max, List::copyOf);
    }
    public static <S, T> CollectionAssembler<S, T, List<T>> listOf(Assembler<S, T> element, int max) {
        return listOf(element, 1, max);
    }
    public static <S, T> CollectionAssembler<S, T, List<T>> relaxedListOf(Assembler<S, T> element, int max) {
        return relaxedListOf(element, 1, max);
    }
    public static <S, T> CollectionAssembler<S, T, List<T>> listOf(Assembler<S, T> element) {
        return listOf(element, 1, 8);
    }
    public static <S, T> CollectionAssembler<S, T, List<T>> relaxedListOf(Assembler<S, T> element) {
        return relaxedListOf(element, 1, 8);
    }
    public static <S, T> CollectionAssembler<S, T, Set<T>> setOf(Assembler<S, T> element, int min, int max) {
        return collectionOf(element, min, max, Set::copyOf);
    }
    public static <S, T> CollectionAssembler<S, T, Set<T>> relaxedSetOf(Assembler<S, T> element, int min, int max) {
        return relaxedCollectionOf(element, min, max, Set::copyOf);
    }
    public static <S, T> CollectionAssembler<S, T, Set<T>> setOf(Assembler<S, T> element, int max) {
        return setOf(element, 1, max);
    }
    public static <S, T> CollectionAssembler<S, T, Set<T>> relaxedSetOf(Assembler<S, T> element, int max) {
        return relaxedSetOf(element, 1, max);
    }
    public static <S, T> CollectionAssembler<S, T, Set<T>> setOf(Assembler<S, T> element) {
        return setOf(element, 1, 8);
    }
    public static <S, T> CollectionAssembler<S, T, Set<T>> relaxedSetOf(Assembler<S, T> element) {
        return relaxedSetOf(element, 1, 8);
    }


    //ASSETS
    private record Delegate<S>(CommandTemplate<S> template, Map<String, String> namesMap) {}


    //FIELDS
    private final int min_, max_;
    private final boolean relaxedMode_;
    private final Contextualizer<S, T> contextualizer_;
    private final Delegate<S>[] delegates_;
    private final Function<Collection<T>, C> composer_;


    //CONSTRUCTOR
    private CollectionAssembler(int min, int max, boolean relaxedMode, Assembler<S, T> delegate, Function<Collection<T>, C> composer) {
        if (max < 1) { throw new IllegalArgumentException("max must be >= 1"); }
        if (min < 0) { throw new IllegalArgumentException("min must be >= 0"); }
        if (max < min) { throw new IllegalArgumentException("max must be >= min"); }

        this.min_ = min;
        this.max_ = max;
        this.relaxedMode_ = relaxedMode;
        this.composer_ = composer;

        this.delegates_ = new Delegate[this.max_];
        CompiledAssembler<S, T> compiledAssembler = CompiledAssembler.of(delegate);
        CommandTemplate<S> template = compiledAssembler.template();
        this.contextualizer_ = compiledAssembler.contextualizer();
        for (int i = 0; i < this.max_; i++) {
            CommandTemplate<S> clone = template.clone();
            final int index = i;
            Map<String, String> namesMap = renameTemplate(clone, s -> s + index);
            this.delegates_[i] = new Delegate<>(clone, namesMap);
        }
    }


    @Override
    public CommandTemplate<S> argumentTemplate() {
        List<CommandTemplate<S>> roots = new ArrayList<>();

        // "none" route to pass an empty list
        if (this.min_ < 1) {
            roots.add(CommandTemplate.<S>literal("none").end());
        }

        CommandTemplate<S> first = this.delegates_[0].template();

        // If max == 1, just take one element.
        if (this.max_ == 1) {
            roots.add(first.clone());
            return CommandTemplate.merge(roots.toArray(new CommandTemplate[0]));
        }

        // "only" route to pass only one element
        if (this.min_ < 2 && this.max_ > 0) {
            CommandTemplate<S> tmp = CommandTemplate.<S>literal("only").end();
            tmp.append(first);
            roots.add(tmp);
        }

        //"and" route for more than two elements
        CommandTemplate<S> last = this.delegates_[this.max_ - 1].template();
        CommandTemplate<S> andTemplate = CommandTemplate.<S>literal("and").end();
        andTemplate.append(last);
        CommandTemplate<S> tmp = first.clone();

        for (int i = 1; i < (this.delegates_.length - 1); i++) {
            CommandTemplate<S> del = this.delegates_[i].template();
            CommandTemplate<S> append = (i >= this.min_)
                    ? CommandTemplate.merge(andTemplate, del)
                    : del;
            tmp.append(append);
        }
        tmp.append(andTemplate);
        roots.add(tmp);

        return CommandTemplate.merge(roots.toArray(new CommandTemplate[0]));
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

            T elm = this.contextualizer_.contextualize(context, new Components(compMap));
            list.add(elm);
        }

        return this.composer_.apply(list);
    }


    private static <S> Map<String, String> renameTemplate(CommandTemplate<S> temp, Function<String, String> renamer) {
        Deque<CommandTemplate.Element<S>> stack = new ArrayDeque<>(temp.trees());
        List<CommandTemplate.Forward<S>> forwards = new ArrayList<>();
        Map<String, String> namesMap = new HashMap<>();

        while (!stack.isEmpty()) switch (stack.pollLast()) {
            case CommandTemplate.Node<S> n -> {
                if (n instanceof CommandTemplate.Argument<?>) {
                    String old = n.label();
                    String rename = renamer.apply(old);
                    n.rename(rename);
                    namesMap.put(old, rename);
                }
                for (CommandTemplate.Element<S> c : n.children()) { stack.addLast(c); }
            }
            case CommandTemplate.Forward<S> f -> forwards.add(f);
            case CommandTemplate.Exit<S> e -> {}
        }

        forwards.forEach(f -> f.reforward(namesMap.get(f.forwardsTo())));

        return namesMap;
    }
}