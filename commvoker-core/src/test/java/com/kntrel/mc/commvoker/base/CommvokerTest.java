package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.command.Command;
import com.kntrel.mc.commvoker.mock.MockCommvoker;
import com.mojang.brigadier.CommandDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.kntrel.mc.commvoker.test.Assertions.*;

public class CommvokerTest {

    /* ---------- 1. plain methods: names turn into literals ---------------- */
    @Command                     // -> /foo
    public void foo() { }

    @Command                     // -> /foo <bar>
    public void foo(String bar) { }

    /* ---------- 2. @Command on a class prefixes its methods --------------- */
    @Command("clazz")            // -> /clazz ...
    public static class CommandClass {
        @Command                 // -> /clazz sub1
        public void sub1() { }

        @Command                 // -> /clazz sub2 <arg>
        public void sub2(String arg) { }
    }

    /* ---------- 3. extend = true keeps the outer literal only ------------- */
    @Command("foo")              // -> /foo <bar>
    public static class FooExtend {
        @Command(extend = true)
        public void inner(String bar) { }
    }

    /* ----------  outer argument “consumes” first parameter --------- */
    @Command("foo {arg1}")       // -> /foo <arg1> inner
    public static class FooArgConsume {
        @Command
        public void inner(String bar) { }
    }

    @Command("foo {arg1}")       // -> /foo <arg1> inner <arg2>
    public static class FooArgConsumePlus {
        @Command
        public void inner(String bar, String arg2) { }
    }

    /* ---------- 6. explicit literals override names ---------------------- */
    @Command("loreim")           // -> /loreim ipsum <bar> <arg2>
    public static class FooOverride {
        @Command("ipsum")
        public void inner(String bar, String arg2) { }
    }

    @Command("{loreim} {ipsum}")      // -> /snake_case <loreim> <ipsum>
    public static class SnakeCase {
        @Command(extend = true)
        public void root(String bar, String arg2) { }

        @Command
        public void subCommand(String bar, String arg2, String arg3) { }
    }

    @Command("roots")
    public static class RootCommand {
        @Command(value = "{a} {b}", extend = true)
        public void root1(String a, String b) {}
        @Command(value = "{a} {b} {c}", extend = true)
        public void root2(String a, String b, String c) {}
    }

    /* --------------------------------------------------------------------- */

    private MockCommvoker commvoker;

    @BeforeEach
    void setUp() {
        commvoker = new MockCommvoker();
    }

    /* ---------------------------------------------------------------------
     *  Helper: assert that the dispatcher advertises a particular usage
     * ------------------------------------------------------------------- */


    /* ---------------------------------------------------------------------
     *  Individual test cases
     * ------------------------------------------------------------------- */

    @Test
    void basicMethodNamesBecomeLiterals() {
        commvoker.register(this);

        CommandDispatcher<Object> d = commvoker.getCommandDispatcher();
        assertHasUsage(d, "foo");
        assertHasUsage(d, "foo <bar>");
    }

    @Test
    void nestedCommandClassUsesItsLiteral() {
        commvoker.register(new CommandClass());

        CommandDispatcher<Object> d = commvoker.getCommandDispatcher();
        assertHasUsage(d, "clazz sub1");
        assertHasUsage(d, "clazz sub2 <arg>");
    }

    @Test
    void extendTrueSkipsInnerLiteral() {
        commvoker.register(new FooExtend());

        CommandDispatcher<Object> d = commvoker.getCommandDispatcher();
        assertHasUsage(d, "foo <bar>");
    }

    @Test
    void classArgumentIsConsumedByFirstMethodParam() {
        commvoker.register(new FooArgConsume());

        CommandDispatcher<Object> d = commvoker.getCommandDispatcher();
        assertHasUsage(d, "foo <arg1> inner");
    }

    @Test
    void classArgumentAndExtraMethodArgs() {
        commvoker.register(new FooArgConsumePlus());

        CommandDispatcher<Object> d = commvoker.getCommandDispatcher();
        assertHasUsage(d, "foo <arg1> inner <arg2>");
    }

    @Test
    void explicitLiteralOverridesNames() {
        commvoker.register(new FooOverride());

        CommandDispatcher<Object> d = commvoker.getCommandDispatcher();
        assertHasUsage(d, "loreim ipsum <bar> <arg2>");
    }
    @Test
    void namesToSnakeCase() {
        commvoker.register(new SnakeCase());

        CommandDispatcher<Object> d = commvoker.getCommandDispatcher();
        assertHasUsage(d, "snake_case <loreim> <ipsum>");
        assertHasUsage(d, "snake_case <loreim> <ipsum> sub_command <arg3>");

    }

    @Test
    void overlappingArguments() {
        commvoker.register(new RootCommand());

        CommandDispatcher<Object> d = commvoker.getCommandDispatcher();
        assertHasUsage(d, "roots <a> <b>");
        assertHasUsage(d, "roots <a> <b> <c>");

    }

    /* ---------------------------------------------------------------------
     *  One big integration test: multiple holders at once
     * ------------------------------------------------------------------- */
    @Test
    void multipleRegistrationsWorkTogether() {
        commvoker.register(this);
        commvoker.register(new CommandClass());
        commvoker.register(new FooExtend());
        commvoker.register(new FooArgConsumePlus());
        commvoker.register(new FooOverride());
        commvoker.register(new SnakeCase());

        CommandDispatcher<Object> d = commvoker.getCommandDispatcher();

        assertHasUsage(d, "foo");
        assertHasUsage(d, "clazz sub2 <arg>");
        assertHasUsage(d, "foo <bar>");
        assertHasUsage(d, "foo <arg1> inner <arg2>");
        assertHasUsage(d, "loreim ipsum <bar> <arg2>");
        assertHasUsage(d, "snake_case <loreim> <ipsum>");
        assertHasUsage(d, "snake_case <loreim> <ipsum> sub_command <arg3>");
    }
}
