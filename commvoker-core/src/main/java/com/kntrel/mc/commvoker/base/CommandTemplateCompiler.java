package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.argument.binding.NameSupplier;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

final class CommandTemplateCompiler<S> {

    //STATIC API
    static <S> CommandTreeGate<S> compile(CommandTemplate<? super S> root, NameSupplier nameSupplier, Command<S> command) {
        return new CommandTemplateCompiler<S>(nameSupplier, command).compile(root);
    }

    static <S> CommandTreeGate<S> compile(CommandTemplate<? super S> root, NameSupplier nameSupplier) {
        return new CommandTemplateCompiler<S>(nameSupplier).compile(root);
    }


    //FIELDS
    private final NameSupplier nameSupplier;
    private final Command<S> command;
    private final Map<CommandTemplate.Node<? super S>, CommandNode<S>> memo = new IdentityHashMap<>();
    private final Deque<CommandTemplate.Node<? super S>> ancestors = new ArrayDeque<>();
    private final List<CommandNode<S>> leaves = new ArrayList<>();
    private Map<String, ? extends List<CommandTemplate.Node<? super S>>> byLabel;
    private boolean used = false;


    //CONSTRUCTORS
    CommandTemplateCompiler(NameSupplier nameSupplier, Command<S> command) {
        this.nameSupplier = Objects.requireNonNull(nameSupplier, "nameSupplier");
        this.command = command; // may be null
    }
    CommandTemplateCompiler(NameSupplier nameSupplier) {
        this(nameSupplier, null);
    }


    //UTILITY
    CommandTreeGate<S> compile(CommandTemplate<? super S> tmp) {
        Objects.requireNonNull(tmp, "template");
        if (this.used) {
            throw new IllegalStateException("This compiler instance has already been used.");
        }
        this.used = true;

        // Pass 0: collect all nodes and enforce global uniqueness of labels
        this.byLabel = indexByLabel(tmp);

        // Pass 1: build nodes (children & targets first), wire redirects, return built roots
        List<CommandNode<S>> roots = tmp.trees().stream()
                .map(this::buildNode)
                .toList();
        CommandTreeGate<S> result = new CommandTreeGate<>(roots, this.leaves);

        // best effort cleanup (helps GC if the instance lingers)
        clear();
        return result;
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
                    throw new IllegalStateException("Duplicate argument node labeled '" + label + "' found in template");
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
    private CommandNode<S> buildNode(CommandTemplate.Node<? super S> node) {
        // memoization
        CommandNode<S> cached = this.memo.get(node);
        if (cached != null) return cached;

        // cycle guard (also protects from accidental structural cycles)
        if (this.ancestors.contains(node)) {
            throw new IllegalStateException("Cycle detected entering node '" + node.label() + "'");
        }
        this.ancestors.addLast(node);

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
                ? RequiredArgumentBuilder.argument(this.nameSupplier.supply(node.label()), arg.argumentType())
                : LiteralArgumentBuilder.literal(node.label());

        // Build normal children first (so we can attach CommandNode<S> instances)
        List<CommandNode<S>> builtChildren = children.stream()
                .map(this::buildNode)
                .collect(Collectors.toCollection(ArrayList::new));

        // Resolving forwards
        for (CommandTemplate.Forward<? super S> forward : forwards) {
            String targetLabel = forward.forwardsTo();

            List<CommandTemplate.Node<? super S>> targets = this.byLabel.get(targetLabel);
            if (targets == null || targets.isEmpty()) {
                throw new IllegalStateException("Forward from '" + node.label() + "' points to unknown label '" + targetLabel + "'");
            }

            int index = forward.occurrence();
            if (index <= targets.size()) { index = targets.size() - 1; }
            CommandTemplate.Node<? super S> target = targets.get(index);

            if (ancestors.contains(target)) {
                throw new IllegalStateException("Forward from '" + node.label() + "' points to ancestor '" + target.label() + "'");
            }
            builtChildren.add(buildNode(target));
        }

        Predicate<? super S> req = node.requirement();
        if (req != null) { builder.requires((Predicate<S>) req); }

        if (node instanceof CommandTemplate.Argument<? super S> arg) {
            SuggestionProvider<? extends S> sug = ((CommandTemplate.Argument<S>) arg).suggestionProvider();
            if (sug != null) { ((RequiredArgumentBuilder<S, ?>) builder).suggests((SuggestionProvider<S>) sug); }
        }

        if (this.command != null && isLeave) { builder.executes(this.command); }
        final CommandNode<S> self = builder.build();
        builtChildren.forEach(self::addChild);
        if (isLeave) { this.leaves.add(self); }

        this.memo.put(node, self);
        this.ancestors.removeLast();
        return self;
    }

    private void clear() {
        byLabel = null;
        memo.clear();
        ancestors.clear();
        leaves.clear();
    }
}