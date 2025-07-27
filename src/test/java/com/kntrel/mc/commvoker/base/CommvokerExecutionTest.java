package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.annotation.Command;
import com.kntrel.mc.commvoker.mock.MockCommvoker;
import org.junit.jupiter.api.*;
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

    /* ------------------------------------------------------------------
     *  Test‑fixture boiler‑plate
     * ---------------------------------------------------------------- */
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
}