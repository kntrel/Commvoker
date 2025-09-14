package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.command.Command;
import com.kntrel.mc.commvoker.argument.binder.ArgumentBinder;
import com.kntrel.mc.commvoker.provided.ArgumentBindings;
import com.kntrel.mc.commvoker.command.CommandPatternToken;
import com.kntrel.mc.commvoker.exception.BadCommandMethodException;
import com.kntrel.mc.commvoker.mock.MockImplicit;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.*;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static com.kntrel.mc.commvoker.test.Assertions.*;

public class CommandParserParseTest {

    /* -------------- Mock commands ------------------*/
    @Command("foo")
    void foo() {}

    @Command("foo bar")
    void foo1() {}

    @Command("foo")
    void foo(String bar1, String bar2) {}

    @Command("test1 * {last}")
    void test1(String a, String b, String c, String d) {}

    @Command("test2 {first} *")
    void test2(String a, String b, String c, String d) {}

    @Command("test3 {first}")
    void test3(String a, String b, String c, String d) {}

    @Command("test4 {first} * {last}")
    void test4(String a, String b, String c, String d) {}

    @Command("test5 {first} * * * {last}")
    void test5(String a, String b, String c, String d) {}

    @Command("test6 {first} * * {} {last}")
    void test6(String a, String b, String c, String d) {}

    @Command("test7 {first} * * * {} {last}")
    void test7(String a, String b, String c, String d) {}

    @Command("test8 * foo")
    void test8(String a, String b, String c, String d) {}

    @Command("test9 * foo *")
    void test9(String a, String b, String c, String d) {}

    @Command("test10 * foo {bar} *")
    void test10(String a, String b, String c, String d) {}

    @Command("test11 * foo bar")
    void test11(@MockImplicit Object src, String a, String b, String c, String d) {}

    /* -----------------------------------------------*/

    private final CommandParser<?> parser;

    
    public CommandParserParseTest() {
        ArgumentResolverImpl<Object> resolver = new ArgumentResolverImpl<>();
        ArgumentBindings.all().forEach(resolver::register);
        resolver.register(ArgumentBinder.<Object, Object>implicit(ExecutionContext::source)
                .toAnnotation(MockImplicit.class)
                .toClass(Object.class)
                .bind());
        this.parser = new CommandParser<>(resolver);
    }


    @Test void parse_noArgs() {
        CommandNode<?> subject = assertCommandMethod("foo");
        assertEqualTrees(literal("foo").build(), subject);

        subject = assertCommandMethod("foo1");
        assertEqualTrees(literal("foo").then(literal("bar")).build(), subject);
    }

    @Test void parse_implicitWildcard() {
        CommandNode<?> subject = assertCommandMethod("foo", String.class, String.class);
        CommandNode<?> against = literal("foo")
            .then(
                argument("bar1", string())
                .then(
                    argument("bar2", greedyString())
                )
            ).build();

        assertEqualTrees(against, subject);
    }

    @Test void parse_wildcardWIthTrailing() {
        CommandNode<?> subject = assertCommandMethod("test1", String.class, String.class, String.class, String.class);
        CommandNode<?> against = literal("test1")
            .then(
                argument("a", string())
                .then(
                    argument("b", string())
                    .then(
                        argument("c", string())
                        .then(
                            argument("last", greedyString())
                        )
                    )
                )
            ).build();

        assertEqualTrees(against, subject);
    }

    @Test void parse_wildcardWIthLeading() {
        CommandNode<Object> tail = argument("first", string())
            .then(
                argument("b", string())
                .then(
                    argument("c", string())
                    .then(
                        argument("d", greedyString())
                    )
                )
            ).build();

        CommandNode<?> subject = assertCommandMethod("test2", String.class, String.class, String.class, String.class);
        assertEqualTrees(literal("test2").then(tail).build(),subject);

        subject = assertCommandMethod("test3", String.class, String.class, String.class, String.class);
        assertEqualTrees(literal("test3").then(tail).build(),subject);
    }

    @Test void parse_wildcardInTheMiddle() {
        CommandNode<?> subject = assertCommandMethod("test4", String.class, String.class, String.class, String.class);
        CommandNode<?> against = literal("test4")
            .then(
                argument("first", string())
                .then(
                    argument("b", string())
                    .then(
                        argument("c", string())
                        .then(
                                argument("last", greedyString())
                        )
                    )
                )
            ).build();

        assertEqualTrees(against, subject);
    }

    @Test void parse_0argWildcardInTheMiddle() {

        CommandNode<Object> tail = argument("first", string())
            .then(
                argument("b", string())
                .then(
                    argument("c", string())
                    .then(
                            argument("last", greedyString())
                    )
                )
            ).build();

        CommandNode<?> subject = assertCommandMethod("test5", String.class, String.class, String.class, String.class);
        assertEqualTrees(literal("test5").then(tail).build(), subject);

        subject = assertCommandMethod("test6", String.class, String.class, String.class, String.class);
        assertEqualTrees(literal("test6").then(tail).build(), subject);
    }

    @Test void parse_tooManyArgs(TestReporter reporter) {
        Method m = getMethod("test7", String.class, String.class, String.class, String.class);
        Command ann = getAnnotation(m);
        var tokens = assertValidTokens(ann.value());

        BadCommandMethodException ex = assertThrows(BadCommandMethodException.class, () -> this.parser.brigadierCommand(tokens, m, this));
        assertEquals(ex.getMethod(), m);

        reporter.publishEntry(ex.getMessage());
    }

    @Test void parse_literalsInTheMiddle() {
        CommandNode<Object> against = literal("test8")
            .then(
                argument("a", string())
                .then(
                    argument("b", string())
                    .then(
                        argument("c", string())
                        .then(
                            argument("d", string())
                            .then(
                                literal("foo")
                            )
                        )
                    )
                )
            ).build();
        CommandNode<?> subject = assertCommandMethod("test8", String.class, String.class, String.class, String.class);
        assertEqualTrees(against, subject);

        against = literal("test9")
            .then(
                argument("a", string())
                .then(
                    literal("foo")
                    .then(
                        argument("b", string())
                        .then(
                            argument("c", string())
                            .then(
                                argument("d", greedyString())
                            )
                        )
                    )
                )
            ).build();
        subject = assertCommandMethod("test9", String.class, String.class, String.class, String.class);
        assertEqualTrees(against, subject);

        against = literal("test10")
            .then(
                argument("a", string())
                .then(
                    literal("foo")
                    .then(
                        argument("bar", string())
                        .then(
                            argument("c", string())
                            .then(
                                argument("d", greedyString())
                            )
                        )
                    )
                )
            ).build();
        subject = assertCommandMethod("test10", String.class, String.class, String.class, String.class);
        assertEqualTrees(against, subject);
    }

    @Test void parse_withVirtual() {
        CommandNode<Object> against = literal("test11")
            .then(
                argument("a", string())
                .then(
                    argument("b", string())
                    .then(
                        argument("c", string())
                        .then(
                            argument("d", string())
                            .then(
                                literal("foo")
                                .then(
                                    literal("bar")
                                )
                            )
                        )
                    )
                )
            ).build();

        CommandNode<?> subject = assertCommandMethod("test11", Object.class, String.class, String.class, String.class, String.class);
        assertEqualTrees(against, subject);
    }

    private Method getMethod(String name, Class<?>... args) {
        return assertDoesNotThrow(() -> this.getClass().getDeclaredMethod(name, args));
    }
    private static Command getAnnotation(Method method) {
        Command ann = method.getAnnotation(Command.class);
        assertNotNull(ann);
        return ann;
    }
    private CommandPatternToken[] assertValidTokens(String pattern) {
        return assertDoesNotThrow(() -> parser.tokenize(pattern));
    }
    private CommandNode<?> assertCommandMethod(String name, Class<?>... args) {
        Method m = getMethod(name, args);
        Command annotation = getAnnotation(m);
        var tokens = assertValidTokens(annotation.value());
        LiteralArgumentBuilder<?> tree = assertDoesNotThrow(() -> parser.brigadierCommand(tokens, m, this).tree());
        return tree.build();
    }
}
