package com.kntrel.mc.commvoker.argument.descriptor;

import com.mojang.brigadier.tree.CommandNode;
import java.util.*;

public record CommandTreeGate<S>(List<CommandNode<S>> roots, List<CommandNode<S>> leaves) {

    public static <S> CommandTreeGate<S> ofTree(CommandNode<S> root) {
        Objects.requireNonNull(root, "roots");

        List<CommandNode<S>> leaves = new ArrayList<>();
        Deque<CommandNode<S>> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            CommandNode<S> node = stack.pop();
            Collection<CommandNode<S>> children = node.getChildren();

            if (children == null || children.isEmpty()) {
                leaves.add(node);
            } else {
                for (CommandNode<S> child : children) {
                    stack.push(child);
                }
            }
        }

        return new CommandTreeGate<>(List.of(root), List.copyOf(leaves));
    }

    public CommandTreeGate<S> append(CommandTreeGate<S> next) {
        this.leaves().forEach(l -> next.roots().forEach(l::addChild));
        return new CommandTreeGate<>(this.roots(), next.leaves());
    }

}
