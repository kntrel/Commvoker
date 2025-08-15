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

final class CommandTemplateCompiler {

    private CommandTemplateCompiler() {}

    static <S> CommandTreeGate<S> compile(CommandTemplate.Node<? super S> root, NameSupplier nameSupplier, Command<S> command) {
        Objects.requireNonNull(root, "root");

        // Pass 0: collect all nodes and enforce global uniqueness of labels
        Map<String, CommandTemplate.Node<? super S>> byLabel = indexByLabel(root);

        // Pass 1: build nodes (children & targets first), wire redirects, return built root
        Map<CommandTemplate.Node<? super S>, CommandNode<S>> built = new IdentityHashMap<>();
        Deque<CommandTemplate.Node<? super S>> stack = new ArrayDeque<>();
        List<CommandNode<S>> leaves = new ArrayList<>();
        CommandNode<S> r = buildNode(root, byLabel, built, stack, nameSupplier, leaves, command);

        return new CommandTreeGate<>(r, leaves);
    }
    static <S> CommandTreeGate<S> compile(CommandTemplate.Node<? super S> root, NameSupplier nameSupplier) {
        return compile(root, nameSupplier, null);
    }


    /* -------------------- pass 0: index & validate -------------------- */

    private static <S> Map<String, CommandTemplate.Node<? super S>> indexByLabel(CommandTemplate.Node<? super S> root) {
        Map<String, CommandTemplate.Node<? super S>> byLabel = new HashMap<>();
        Deque<CommandTemplate.Node<? super S>> dfs = new ArrayDeque<>();
        dfs.add(root);

        while (!dfs.isEmpty()) {
            CommandTemplate.Node<? super S> n = dfs.pollLast();
            String label = n.label();
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("Node with empty label encountered");
            }
            CommandTemplate.Node<? super S> prev = byLabel.putIfAbsent(label, n);
            if (prev != null) {
                throw new IllegalStateException("Duplicate node label '" + label + "' found in template");
            }
            for (CommandTemplate<? super S> child : n.children()) switch (child) {
                case CommandTemplate.Node<? super S> ch -> dfs.addLast(ch);
                case CommandTemplate.Forward<? super S> ch -> {}                // nothing to index here; target will be validated during build
                default -> throw new IllegalStateException("Unsupported child kind: " + child.getClass().getSimpleName());
            }
        }
        return byLabel;
    }

    /* -------------------- pass 1: build -------------------- */

    //@SuppressWarnings("unchecked")
    private static <S> CommandNode<S> buildNode(
            CommandTemplate.Node<? super S> node,
            Map<String, CommandTemplate.Node<? super S>> byLabel,
            Map<CommandTemplate.Node<? super S>, CommandNode<S>> built,
            Deque<CommandTemplate.Node<? super S>> ancestors,
            NameSupplier nameSupplier,
            List<CommandNode<S>> leaves,
            Command<S> command
    ) {
        // memoization
        CommandNode<S> cached = built.get(node);
        if (cached != null) return cached;

        // cycle guard (should not happen if forward-to-ancestor is blocked,
        // but protects from accidental structural cycles)
        if (ancestors.contains(node)) {
            throw new IllegalStateException("Cycle detected entering node '" + node.label() + "'");
        }
        ancestors.addLast(node);

        // Check children shape: either a single Forward, or zero-or-more normal Node children
        List<CommandTemplate.Node<? super S>> children = new ArrayList<>();
        List<CommandTemplate.Forward<? super S>> forwards = new ArrayList<>();
        for (CommandTemplate<? super S> child : node.children()) switch (child) {
            case CommandTemplate.Node<? super S> ch -> children.add(ch);
            case CommandTemplate.Forward<? super S> ch -> forwards.add(ch);
            default -> throw new IllegalStateException("Unsupported child kind: " + child.getClass().getSimpleName());
        }

        // Build children first (so we have CommandNode<S> instances to attach)
        List<CommandNode<S>> builtChildren = new ArrayList<>(children.size());
        for (CommandTemplate.Node<? super S> c : children) {
            builtChildren.add(buildNode(c, byLabel, built, ancestors, nameSupplier, leaves, command));
        }

        // If forward exists, resolve and validate
        CommandNode<S> redirectTarget = null;
        if (!forwards.isEmpty()) {
            // must be exactly one and no other children
            if (forwards.size() > 1 || !children.isEmpty()) {
                throw new IllegalStateException("Node '" + node.label() + "' has Forward plus other children");
            }

            CommandTemplate.Forward<? super S> fw = forwards.getFirst();
            String targetLabel = fw.forwardsTo();

            CommandTemplate.Node<? super S> target = byLabel.get(targetLabel);
            if (target == null) {
                throw new IllegalStateException("Forward from '" + node.label() + "' points to unknown label '" + targetLabel + "'");
            }
            if (ancestors.contains(target)) {
                throw new IllegalStateException("Forward from '" + node.label() + "' points to ancestor '" + target.label() + "'");
            }
            // Ensure target is built first
            redirectTarget = buildNode(target, byLabel, built, ancestors, nameSupplier, leaves, command);
        }

        // Build this node's builder and wire either children or redirect
        ArgumentBuilder<S, ?> builder = (node instanceof CommandTemplate.Argument<? super S> arg)
                ? RequiredArgumentBuilder.argument(nameSupplier.supply(node.label()), arg.argumentType())
                : LiteralArgumentBuilder.literal(node.label());

        Predicate<? super S> req = node.requirement();
        if (req != null) {
            builder.requires((Predicate<S>) req);
        }

        final CommandNode<S> self;
        if (redirectTarget != null) {
            // redirect: no children allowed
            builder.redirect(redirectTarget);
            self = builder.build();
        } else if (builtChildren.isEmpty()) {
            // normal: build once, then attach built children to the node
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

        built.put(node, self);
        ancestors.removeLast();
        return self;
    }
}