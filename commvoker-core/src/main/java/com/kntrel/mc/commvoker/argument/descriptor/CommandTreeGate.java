package com.kntrel.mc.commvoker.argument.descriptor;

import com.mojang.brigadier.tree.CommandNode;
import java.util.*;
import java.util.function.Consumer;

/**
 * Internal API. Public for framework wiring, but not part of the supported external API.
 */
public record CommandTreeGate<S>(List<CommandNode<S>> roots, List<CommandNode<S>> leaves) {

    public static <S> CommandTreeGate<S> ofTree(CommandNode<S> root) {
        Objects.requireNonNull(root, "roots");
        return new CommandTreeGate<>(List.of(root), forEach(null, root));
    }

    public CommandTreeGate<S> append(CommandTreeGate<S> next) {
        this.leaves().forEach(l -> next.roots().forEach(l::addChild));
        return new CommandTreeGate<>(this.roots(), next.leaves());
    }
    public List<CommandNode<S>> allNodes() {
        List<CommandNode<S>> all = new ArrayList<>();
        this.forEach(all::add);
        return all;
    }
    public void forEach(Consumer<CommandNode<S>> action) {
        this.roots().forEach(r -> forEach(action, r));
    }


    //HELPERS
    private static <S> List<CommandNode<S>> forEach(Consumer<CommandNode<S>> action, CommandNode<S> root) {
        Objects.requireNonNull(root, "roots");

        List<CommandNode<S>> leaves = new ArrayList<>();
        Deque<CommandNode<S>> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            CommandNode<S> node = stack.pop();
            if (action != null) { action.accept(node); }
            Collection<CommandNode<S>> children = node.getChildren();

            if (children == null || children.isEmpty()) {
                leaves.add(node);
            } else {
                for (CommandNode<S> child : children) {
                    stack.push(child);
                }
            }
        }

        return leaves;
    }
}
