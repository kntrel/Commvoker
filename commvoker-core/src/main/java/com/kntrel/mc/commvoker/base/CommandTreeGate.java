package com.kntrel.mc.commvoker.base;

import com.mojang.brigadier.tree.CommandNode;
import java.util.*;

record CommandTreeGate<S>(CommandNode<S> root, List<CommandNode<S>> leaves) {

    public static <S> CommandTreeGate<S> ofTree(CommandNode<S> root) {
        Objects.requireNonNull(root, "root");

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

        return new CommandTreeGate<>(root, List.copyOf(leaves));
    }

}
