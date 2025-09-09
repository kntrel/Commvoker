package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.argument.binding.NameSupplier;
import com.kntrel.mc.commvoker.argument.binding.Suggester;
import com.kntrel.mc.commvoker.argument.descriptor.*;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

final class ArgumentDescriptorCompiler<S> {

    //ASSETS
    private record CompileScope<S>(
            NameSupplier nameSupplier,
            Map<CommandTemplate.Node<? super S>, CommandNode<S>> builtNodes,
            Deque<CommandTemplate.Node<? super S>> ancestors,
            List<CommandNode<S>> leaves,
            Map<String, ? extends List<CommandTemplate.Node<? super S>>> byLabel
    ) {}


    //FIELDS
    public final ArgumentParser<S>[] argumentParsers_;
    private final IdentityHashMap<CommandNode<S>, CompiledArgumentDescriptor<S, ?>> descriptorMap_;
    private final ThreadLocal<CompileScope<S>> threadLocal_;


    //CONSTRUCTORS
    ArgumentDescriptorCompiler(ArgumentParser<S>[] argumentParsers) {
        this.argumentParsers_ = argumentParsers;
        this.descriptorMap_ = new IdentityHashMap<>();
        this.threadLocal_ = new ThreadLocal<>();
    }


    //UTILITY
    <T> CompiledArgumentDescriptor<? super S, T> compile(TypedArgumentDescriptor<? super S, T> descriptor, NameSupplier nameSupplier, Command<S> command) {
        Objects.requireNonNull(descriptor, "argument descriptor");
        CommandTemplate<? super S> tmp = descriptor.template();
        CompileScope<S> scope = new CompileScope<>(
                nameSupplier,
                new HashMap<>(),
                new ArrayDeque<>(),
                new ArrayList<>(),
                indexByLabel(tmp)
        );
        this.threadLocal_.set(scope);

        // Pass 1: build nodes (children & targets first), wire redirects, return built roots
        List<CommandNode<S>> roots = tmp.trees().stream()
                .map(n -> buildNode(n, command))
                .toList();
        CompiledArgumentDescriptor<S, T> compiled = CompiledArgumentDescriptor.of((TypedArgumentDescriptor<S, T>) descriptor, new CommandTreeGate<>(roots, scope.leaves()));
        roots.forEach(r -> this.descriptorMap_.put(r, compiled));
        this.threadLocal_.remove();
        return compiled;
    }
    <T> CompiledArgumentDescriptor<? super S, T> compile(TypedArgumentDescriptor<? super S, T> descriptor, NameSupplier nameSupplier) {
        return compile(descriptor, nameSupplier, null);
    }


    //HELPERS
    private Map<String, ? extends List<CommandTemplate.Node<? super S>>> indexByLabel(CommandTemplate<? super S> tmp) {
        Map<String, List<CommandTemplate.Node<? super S>>> map = new HashMap<>();
        Deque<CommandTemplate.Node<? super S>> dfs = new ArrayDeque<>(tmp.trees());
        Set<String> argumentLabels = new HashSet<>();

        while (!dfs.isEmpty()) {
            CommandTemplate.Node<? super S> n = dfs.pollLast();
            String label = n.label();
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("Node with empty label encountered");
            }
            if (n instanceof CommandTemplate.Argument<? super S>) {
                if (argumentLabels.contains(label)) {
                    continue;
                }
                argumentLabels.add(label);
            }
            map.computeIfAbsent(label, l -> new ArrayList<>()).add(n);

            for (CommandTemplate.Element<? super S> c : n.children()) {
                if (c instanceof CommandTemplate.Node<? super S> ch) { dfs.addLast(ch); }
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private CommandNode<S> buildNode(CommandTemplate.Node<? super S> node, Command<S> command) {
        CompileScope<S> scope = this.threadLocal_.get();

        // memoization
        CommandNode<S> cached = scope.builtNodes.get(node);
        if (cached != null) return cached;

        // cycle guard (also protects from accidental structural cycles)
        if (scope.ancestors.contains(node)) {
            throw new IllegalStateException("Cycle detected entering node '" + node.label() + "'");
        }
        scope.ancestors.addLast(node);

        // Partition children: either many Nodes OR exactly one Forward
        List<CommandTemplate.Node<? super S>> children = new ArrayList<>();
        List<CommandTemplate.Forward<? super S>> forwards = new ArrayList<>();
        boolean isLeave = false;
        for (CommandTemplate.Element<? super S> child : node.children()) {
            switch (child) {
                case CommandTemplate.Node<? super S> ch -> children.add(ch);
                case CommandTemplate.Forward<? super S> ch -> forwards.add(ch);
                case CommandTemplate.Exit<? super S> ch -> isLeave = true;
            }
        }

        // Build this node (literal or argument), apply requirement, then wire children/redirect/command
        // Done before children recursion so a nameSupplier name is reserved before the children
        ArgumentBuilder<S, ?> builder = (node instanceof CommandTemplate.Argument<? super S> arg)
                ? RequiredArgumentBuilder.argument(scope.nameSupplier.supply(node.label()), arg.argumentType())
                : LiteralArgumentBuilder.literal(node.label());

        // Build normal children first (so we can attach CommandNode<S> instances)
        List<CommandNode<S>> builtChildren = children.stream()
                .map(n -> buildNode(n, command))
                .collect(Collectors.toCollection(ArrayList::new));

        // Resolving forwards
        for (CommandTemplate.Forward<? super S> forward : forwards) {
            String targetLabel = forward.forwardsTo();

            List<CommandTemplate.Node<? super S>> targets = scope.byLabel.get(targetLabel);
            if (targets == null || targets.isEmpty()) {
                throw new IllegalStateException("Forward from '" + node.label() + "' points to unknown label '" + targetLabel + "'");
            }

            int index = forward.occurrence();
            if (index <= targets.size()) { index = targets.size() - 1; }
            CommandTemplate.Node<? super S> target = targets.get(index);

            if (scope.ancestors.contains(target)) {
                throw new IllegalStateException("Forward from '" + node.label() + "' points to ancestor '" + target.label() + "'");
            }
            builtChildren.add(buildNode(target, command));
        }

        RequirementNode<S> reqNode = new RequirementNode<>();
        builder.requires(reqNode);
        Predicate<? super S> req = node.requirement();
        if (req != null) { reqNode.and(req); }

        if (node instanceof CommandTemplate.Argument<? super S> arg) {
            Suggester<? extends S> sug = ((CommandTemplate.Argument<S>) arg).suggester();
            if (sug != null) { ((RequiredArgumentBuilder<S, ?>) builder).suggests(new SuggesterBridge<>((Suggester<S>) sug, this.argumentParsers_)); }
        }

        if (command != null && isLeave) { builder.executes(command); }
        final CommandNode<S> self = builder.build();
        builtChildren.forEach(self::addChild);
        if (isLeave) { scope.leaves.add(self); }

        scope.builtNodes().put(node, self);
        scope.ancestors().removeLast();
        return self;
    }
}