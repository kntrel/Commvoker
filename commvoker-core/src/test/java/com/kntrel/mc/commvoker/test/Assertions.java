package com.kntrel.mc.commvoker.test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

public final class Assertions {

    private Assertions() {}


    public static void assertEqualTrees(CommandNode<?> a, CommandNode<?> b) {
        assertEquals(a.getName(), b.getName());

        Queue<CommandNode<?>[]> queue = new LinkedList<>();
        queue.add(new CommandNode[] {a, b});

        while (!queue.isEmpty()) {
            a = queue.peek()[0];
            b = queue.poll()[1];

            assertInstanceOf(a.getClass(), b);
            assertEquals(a.getChildren().size(), b.getChildren().size());
            if (a instanceof ArgumentCommandNode<?,?> arg) {
                assertInstanceOf(arg.getType().getClass(), ((ArgumentCommandNode<?, ?>) b).getType());
            }

            for (CommandNode<?> childA : a.getChildren()) {
                CommandNode<?> childB = b.getChild(childA.getName());
                assertNotNull(childB, "No child '" + childA.getName() + "' found");
                queue.add(new CommandNode[]{childA, childB});
            }
        }
    }

    public static void assertHasUsage(CommandDispatcher<Object> d, String expected) {
        Collection<String> usages = Arrays.stream(d.getAllUsage(d.getRoot(), new Object(), false)).toList();
        assertTrue(
                usages.contains(expected),
                () -> "Usage «" + expected + "» not found.\nActual usages: " + usages
        );
    }
}
