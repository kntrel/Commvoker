package com.kntrel.mc.commvoker.requirement;

import com.kntrel.mc.commvoker.command.Command;
import com.kntrel.mc.commvoker.mock.MockCommvoker;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static com.kntrel.mc.commvoker.test.Assertions.*;

public class RequiresTest {

    //ASSETS
    private static final Object SRC = new Object();

    public static class MockRequirement implements Requirement<Object> {
        @Override
        public boolean test(Object o) {
            if (!(o instanceof AtomicInteger ai)) { return false; }
            ai.incrementAndGet();
            return true;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Requires(MockRequirement.class)
    public @interface MetaRequirement {}

    public static class Holder1 {
        @Command("test1")
        @Requires(MockRequirement.class)
        public void test() {}
    }
    public static class Holder2 {
        @Command("test1")
        public void test1() {}

        @Command("test1 test2")
        @Requires(MockRequirement.class)
        public void test2() {}
    }
    public static class Holder3 {
        @Command("test1")
        public void test1() {}

        @Command("test1 test2")
        @Requires(MockRequirement.class)
        public void test2() {}

        @Command("test1 test2 test3")
        public void test3() {}
    }
    public static class Holder4 {
        @Command("test1")
        @MetaRequirement
        public void test() {}
    }


    //FIELDS
    private MockCommvoker commvoker;


    //SETUP
    @BeforeEach void setup() {
        this.commvoker = new MockCommvoker();
    }


    //TESTS
    @Test void requirementPresence() {
        this.commvoker.register(new Holder1());

        CommandNode<Object> root = this.commvoker.getCommandDispatcher().getRoot().getChild("test1");
        assertNotNull(root);

        assertEqualTrees(root, LiteralArgumentBuilder.literal("test1").build());
        assertRequires(root);
    }

    @Test void requirementConflict() {
        this.commvoker.register(new Holder2());

        CommandNode<Object> root = this.commvoker.getCommandDispatcher().getRoot().getChild("test1");
        assertNotNull(root);

        CommandNode<Object> sub = root.getChild("test2");
        assertNotNull(sub);

        assertEqualTrees(root, LiteralArgumentBuilder.literal("test1")
                .then(LiteralArgumentBuilder.literal("test2"))
                .build());

        assertDoesNotRequire(root);
        assertRequires(sub);

        CommandDispatcher<Object> dispatcher = this.commvoker.getCommandDispatcher();
        ParseResults<Object> parseRes = dispatcher.parse("test1 ", SRC);
        CompletableFuture<Suggestions> suggestionsTask = dispatcher.getCompletionSuggestions(parseRes);
        Suggestions suggestions = suggestionsTask.join();
        assertEquals(1, suggestions.getList().size());

        assertDoesNotThrow(() -> dispatcher.execute("test1", SRC));
        assertThrows(CommandSyntaxException.class, () -> dispatcher.execute("test1 test2", SRC));

        String[] usage = dispatcher.getAllUsage(dispatcher.getRoot(), SRC, true);
        assertArrayEquals(new String[] { "test1" }, usage);
    }

    @Test void requirementConflict2() {
        this.commvoker.register(new Holder3());

        CommandNode<Object> root = this.commvoker.getCommandDispatcher().getRoot().getChild("test1");
        assertNotNull(root);

        CommandNode<Object> sub1 = root.getChild("test2");
        assertNotNull(sub1);

        CommandNode<Object> sub2 = sub1.getChild("test3");
        assertNotNull(sub2);

        assertEqualTrees(root, LiteralArgumentBuilder.literal("test1")
                .then(LiteralArgumentBuilder.literal("test2")
                .then(LiteralArgumentBuilder.literal("test3")))
                .build());

        assertDoesNotRequire(root);
        assertDoesNotRequire(sub1);
        assertDoesNotRequire(sub2);
    }

    @Test void metaRequirement() {
        this.commvoker.register(new Holder4());

        CommandNode<Object> root = this.commvoker.getCommandDispatcher().getRoot().getChild("test1");
        assertNotNull(root);

        assertEqualTrees(root, LiteralArgumentBuilder.literal("test1").build());
        assertRequires(root);
    }


    //ASSERTIONS
    @SuppressWarnings("unchecked")
    void assertRequires(CommandNode<?> node) {
        Predicate<Object> predicate = (Predicate<Object>) node.getRequirement();
        assertNotNull(predicate, "Expected node to have a requirement, but it was null.");

        AtomicInteger ai = new AtomicInteger(0);
        assertTrue(predicate.test(ai), "Expected requirement predicate to pass, but it failed.");
        assertEquals(1, ai.get(), "Expected requirement predicate to increment AtomicInteger once.");
    }
    @SuppressWarnings("unchecked")
    void assertDoesNotRequire(CommandNode<?> node) {
        Predicate<Object> predicate = (Predicate<Object>) node.getRequirement();
        if (predicate == null) { return; }

        AtomicInteger ai = new AtomicInteger(0);
        predicate.test(ai);
        assertEquals(0, ai.get(), "Expected requirement predicate to not increment AtomicInteger.");
    }
}
