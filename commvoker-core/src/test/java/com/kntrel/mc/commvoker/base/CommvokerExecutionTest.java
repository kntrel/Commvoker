package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.annotation.Command;
import com.kntrel.mc.commvoker.argument.bind.ArgumentBinder;
import com.kntrel.mc.commvoker.argument.type.ContextualArgumentType;
import com.kntrel.mc.commvoker.mock.MockCommvoker;
import com.kntrel.util.Priority;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.junit.jupiter.api.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;


class CommvokerExecutionTest {


    @SuppressWarnings("unused")
    static class Holder {

        /* flag toggled by a no‑arg command */
        final AtomicBoolean pingCalled = new AtomicBoolean(false);

        /* captures the single‑arg “hello” command */
        volatile String greetedName = null;

        /* captures two ints that should be added */
        final AtomicInteger sum = new AtomicInteger();

        /* greedy‑string capture */
        volatile String echoed = null;

        @Command("ping")
        public void ping() {                       // -> /ping
            pingCalled.set(true);
        }

        @Command("hello {name}")
        public void hello(String name) {           // -> /hello <name>
            greetedName = name;
        }

        @Command("add {a} {b}")
        public void add(int a, int b) {            // -> /add <a> <b>
            sum.set(a + b);
        }

        @Command("say {msg}")
        public void say(String msg) {              // -> /say <msg...>  (greedy)
            echoed = msg;
        }
    }

    private static class MockContextualArgumentType implements ContextualArgumentType<Object, String, String> {

        @Override
        public String parse(StringReader reader) throws CommandSyntaxException {
            return StringArgumentType.word().parse(reader);
        }

        @Override
        public String contextualize(CommandContext<Object> context, String subject) {
            return context.getSource().getClass().getSimpleName() + "-" + subject;
        }
    }


    private MockCommvoker commvoker;
    private Holder holder;

    /** Brigadier needs some source object; any stub will do. */
    private static final Object SRC = new Object();

    @BeforeEach
    void setUp() {
        commvoker = new MockCommvoker();
        holder    = new Holder();

        commvoker.register(holder);
    }

    /* ------------------------------------------------------------------
     *  Actual invocation tests
     * ---------------------------------------------------------------- */

    @Test
    void pingRuns() {
        assertFalse(holder.pingCalled.get());

        assertDoesNotThrow(() -> commvoker.execute("ping", SRC));

        assertTrue(holder.pingCalled.get(), "`ping()` method was not invoked");
    }

    @Test
    void helloPassesArgument() {
        assertNull(holder.greetedName);

        assertDoesNotThrow(() -> commvoker.execute("hello Alice", SRC));

        assertEquals("Alice", holder.greetedName, "Argument was not propagated");
    }

    @Test
    void addCalculatesSum() {
        assertEquals(0, holder.sum.get());

        assertDoesNotThrow(() -> commvoker.execute("add 7 35", SRC));

        assertEquals(42, holder.sum.get(), "Sum was not correct");
    }

    @Test
    void sayGreedyCapturesRestOfLine() {
        assertNull(holder.echoed);

        /* Note the spaces – everything after 'say ' becomes one String */
        assertDoesNotThrow(() -> commvoker.execute("say the cake is a lie", SRC));

        assertEquals("the cake is a lie", holder.echoed,
                "Greedy string did not consume entire remainder");
    }

    @Test void contextualArgumentTypes() {
        commvoker = new MockCommvoker();
        this.commvoker.getArgumentRegistry().register(
                ArgumentBinder.contextual(MockContextualArgumentType::new)
                        .toClass(String.class)
                        .withPriority(Priority.HIGH)
                        .bind()
        );
        commvoker.register(holder);

        assertDoesNotThrow(() -> commvoker.execute("hello Contextual", new ArrayList<>()));

        assertEquals("ArrayList-Contextual", holder.greetedName, "Argument was not propagated");
    }
}