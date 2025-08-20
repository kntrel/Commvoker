package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.binding.CommandTemplate;
import com.kntrel.mc.commvoker.argument.binding.NameSupplier;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import java.util.*;
import java.util.function.Predicate;

final class CommandTemplateCompiler<S> {

    //STATIC API
    static <S> CommandTreeGate<S> compile(CommandTemplate.Node<? super S> root, NameSupplier nameSupplier, Command<S> command) {
        return new CommandTemplateCompiler<S>(nameSupplier, command).compile(root);
    }

    static <S> CommandTreeGate<S> compile(CommandTemplate.Node<? super S> root, NameSupplier nameSupplier) {
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
    CommandTreeGate<S> compile(CommandTemplate.Node<? super S> root) {
        Objects.requireNonNull(root, "root");
        if (used) {
            throw new IllegalStateException("This compiler instance has already been used.");
        }
        used = true;

        // Pass 0: collect all nodes and enforce global uniqueness of labels
        this.byLabel = indexByLabel(root);

        // Pass 1: build nodes (children & targets first), wire redirects, return built root
        CommandNode<S> r = buildNode(root);
        CommandTreeGate<S> result = new CommandTreeGate<>(r, List.copyOf(leaves));

        // best effort cleanup (helps GC if the instance lingers)
        clear();
        return result;
    }


    //HELPERS
    private Map<String, ? extends List<CommandTemplate.Node<? super S>>> indexByLabel(CommandTemplate.Node<? super S> root) {
        Map<String, List<CommandTemplate.Node<? super S>>> map = new HashMap<>();
        Deque<CommandTemplate.Node<? super S>> dfs = new ArrayDeque<>();
        Set<String> argumentLabels = new HashSet<>();
        dfs.add(root);

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

            for (CommandTemplate<? super S> child : n.children()) {
                switch (child) {
                    case CommandTemplate.Node<? super S> ch -> dfs.addLast(ch);
                    case CommandTemplate.Forward<? super S> ch -> { /* target validated during build */ }
                }
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
        CommandTemplate.Forward<? super S> forward = null;
        for (CommandTemplate<? super S> child : node.children()) {
            switch (child) {
                case CommandTemplate.Node<? super S> ch -> children.add(ch);
                case CommandTemplate.Forward<? super S> ch -> {
                    if (forward != null) {
                        throw new IllegalStateException("Node '" + node.label() + "' has forwards to more than one node");
                    }
                    forward = ch;
                }
            }
        }

        // Build normal children first (so we can attach CommandNode<S> instances)
        List<CommandNode<S>> builtChildren = children.stream().map(this::buildNode).toList();

        // Resolve redirect target if present
        CommandNode<S> redirectTarget = null;
        if (forward != null) {
            if (!children.isEmpty()) {
                throw new IllegalStateException("Node '" + node.label() + "' has Forward plus other children");
            }
            String targetLabel = forward.forwardsTo();

            List<CommandTemplate.Node<? super S>> targets = byLabel.get(targetLabel);
            if (targets == null || targets.isEmpty()) {
                throw new IllegalStateException("Forward from '" + node.label() + "' points to unknown label '" + targetLabel + "'");
            }

            int index = forward.occurrence();
            if (index <= targets.size()) { index = targets.size() - 1; }
            CommandTemplate.Node<? super S> target = targets.get(index);

            if (ancestors.contains(target)) {
                throw new IllegalStateException("Forward from '" + node.label() + "' points to ancestor '" + target.label() + "'");
            }
            redirectTarget = buildNode(target);
        }

        // Build this node (literal or argument), apply requirement, then wire children/redirect/command
        ArgumentBuilder<S, ?> builder = (node instanceof CommandTemplate.Argument<? super S> arg)
                ? RequiredArgumentBuilder.argument(nameSupplier.supply(node.label()), arg.argumentType())
                : LiteralArgumentBuilder.literal(node.label());

        Predicate<? super S> req = node.requirement();
        if (req != null) {
            // safe cast: brigadier requires Predicate<S>, template provides Predicate<? super S>
            builder.requires((Predicate<S>) req);
        }

        final CommandNode<S> self;
        if (redirectTarget != null) {
            builder.then(redirectTarget);
            self = builder.build();
        } else if (builtChildren.isEmpty()) {
            if (command != null) {
                builder.executes(command);
            }
            self = builder.build();
            leaves.add(self);
        } else {
            for (CommandNode<S> chNode : builtChildren) {
                builder.then(chNode);
            }
            self = builder.build();
        }

        memo.put(node, self);
        ancestors.removeLast();
        return self;
    }

    private void clear() {
        byLabel = null;
        memo.clear();
        ancestors.clear();
        leaves.clear();
    }
}