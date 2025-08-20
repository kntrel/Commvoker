package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.command.Command;
import com.kntrel.mc.commvoker.provided.annotations.NotGreedy;
import com.kntrel.mc.commvoker.argument.binder.ArgumentBinder;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import com.kntrel.mc.commvoker.mock.MockCommvoker;
import com.kntrel.mc.commvoker.provided.assemblers.StringAssembler;
import com.kntrel.util.Priority;
import com.mojang.brigadier.context.CommandContext;
import org.junit.jupiter.api.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static com.kntrel.mc.commvoker.test.Assertions.*;


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

        List<String> list = null;

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

        @Command("names {name}")
        public void setNames(List<String> names) {              // -> /say <msg...>  (greedy)
            this.list = names;
        }
    }

    @Command("root")
    public static class RootCommand {
        String a, b, c;

        @Command(value = "{a} {b}", extend = true)
        public void root1(String a, @NotGreedy String b) {
            this.a = a; this.b = b;
        }
        @Command(value = "{a} {b} {c}", extend = true)
        public void root2(String a, String b, String c) {
            this.a = a; this.b = b; this.c = c;
        }
    }

    private static class MockContextualAssembler implements TransformAssembler<Object, String, String> {

        @Override
        public Assembler<? super Object, ? extends String> delegate() {
            return StringAssembler.string();
        }

        @Override
        public String compose(CommandContext<?> ctx, String object) {
            return ctx.getSource().getClass().getSimpleName() + "-" + object;
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

    @Test
    void overlappingArguments() {
        RootCommand cmnd = new RootCommand();
        this.commvoker.register(cmnd);

        assertDoesNotThrow(() -> this.commvoker.execute("root a1 b2", SRC));
        assertEquals("a1", cmnd.a);
        assertEquals("b2", cmnd.b);

        assertDoesNotThrow(() -> this.commvoker.execute("root a3 b4 c5", SRC));
        assertEquals("a3", cmnd.a);
        assertEquals("b4", cmnd.b);
        assertEquals("c5", cmnd.c);
    }


    @Test
    void listArgument() {
        assertNull(holder.list);

        assertHasUsage(commvoker.getCommandDispatcher(), "names <name> and <name7>");
        assertDoesNotThrow(() -> commvoker.execute("names john mike and sarah", SRC));
        assertNotNull(holder.list);
        assertEquals(3, holder.list.size());
        assertEquals("john", holder.list.get(0));
        assertEquals("mike", holder.list.get(1));
        assertEquals("sarah", holder.list.get(2));
    }

    @Test void contextualArgumentTypes() {
        commvoker = new MockCommvoker();
        this.commvoker.getArgumentRegistry().register(
                ArgumentBinder.argumentAssembler(MockContextualAssembler::new)
                        .toClass(String.class)
                        .withPriority(Priority.HIGH)
                        .bind()
        );
        commvoker.register(holder);

        assertDoesNotThrow(() -> commvoker.execute("hello Contextual", new ArrayList<>()));

        assertEquals("ArrayList-Contextual", holder.greetedName, "Argument was not propagated");
    }
}