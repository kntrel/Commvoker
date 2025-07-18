package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.exception.BadCommandTokenException;
import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CommandParserTokenizeTest {

    private final CommandParser<?> parser = new CommandParser<>(new ArgumentTypeResolver<>());

    @Test void tokenize_literal1() {
        var tokens = assertValidTokens("foo");
        assertEquals(1, tokens.length);
        assertEquals(CommandParser.TokenType.LITERAL, tokens[0].type());
        assertEquals("foo", tokens[0].label());
    }

    @Test void tokenize_literal2() {
        var tokens = assertValidTokens("foo bar");
        assertEquals(2, tokens.length);
        assertEquals(CommandParser.TokenType.LITERAL, tokens[0].type());
        assertEquals("foo", tokens[0].label());
        assertEquals(CommandParser.TokenType.LITERAL, tokens[1].type());
        assertEquals("bar", tokens[1].label());
    }

    @Test void tokenize_literals3() {
        String[] labels = new String[10];
        for (int i = 0; i < 10; i++) {
            labels[i] = "literal" + i;
        }

        String pattern = String.join(" ", labels);
        var tokens = assertDoesNotThrow(() -> parser.tokenize(pattern));
        assertEquals(10, tokens.length);

        for (int i = 0; i < 10; i++) {
            assertEquals(CommandParser.TokenType.LITERAL, tokens[i].type());
            assertEquals(labels[i], tokens[i].label());
        }
    }

    @Test void tokenize_invalid() {
        BadCommandTokenException ex = assertThrows(BadCommandTokenException.class, () -> parser.tokenize("foo ba/r"));
        assertEquals("ba/r", ex.getToken());
    }

    @Test void tokenize_arguments_explicit() {
        var tokens1 = assertValidTokens("foo {bar}");
        var tokens2 = assertValidTokens("foo <bar>");
        var tokens3 = assertValidTokens("foo {bar} <bar1>");

        assertEquals(2, tokens1.length);
        assertEquals(2, tokens2.length);
        assertEquals(3, tokens3.length);

        CommandParser.TokenType[] types = new CommandParser.TokenType[] { CommandParser.TokenType.LITERAL, CommandParser.TokenType.ARGUMENT, CommandParser.TokenType.ARGUMENT };
        String[] labels = new String[] { "foo", "bar", "bar1" };

        for (CommandParser.Token[] tokens : Set.of(tokens1, tokens2, tokens3)) {
            for (int i = 0; i < tokens.length; i++) {
                assertEquals(types[i], tokens[i].type());
                assertEquals(labels[i], tokens[i].label());
            }
        }
    }

    @Test void tokenize_arguments_unnamed() {
        var tokens1 = assertDoesNotThrow(() -> parser.tokenize("foo {}"));
        var tokens2 = assertDoesNotThrow(() -> parser.tokenize("foo <>"));
        var tokens3 = assertDoesNotThrow(() -> parser.tokenize("foo {} <bar>"));
        var tokens4 = assertDoesNotThrow(() -> parser.tokenize("foo {} <bar> <>"));
        var tokens5 = assertDoesNotThrow(() -> parser.tokenize("foo {} <bar> <> literal"));

        assertEquals(2, tokens1.length);
        assertEquals(2, tokens2.length);
        assertEquals(3, tokens3.length);
        assertEquals(4, tokens4.length);
        assertEquals(5, tokens5.length);

        CommandParser.TokenType[] types = new CommandParser.TokenType[] { CommandParser.TokenType.LITERAL, CommandParser.TokenType.UNNAMED_ARGUMENT, CommandParser.TokenType.ARGUMENT, CommandParser.TokenType.UNNAMED_ARGUMENT, CommandParser.TokenType.LITERAL };
        String[] labels = new String[] { "foo", null, "bar", null, "literal" };

        for (CommandParser.Token[] tokens : Set.of(tokens1, tokens2, tokens3, tokens4, tokens5)) {
            for (int i = 0; i < tokens.length; i++) {
                assertEquals(types[i], tokens[i].type());
                assertEquals(labels[i], tokens[i].label());
            }
        }
    }

    @Test void tokenize_arguments_wildcard() {
        var tokens = assertValidTokens("foo *");
        assertTokensLabel(tokens, "foo", null);
        assertTokensType(tokens, CommandParser.TokenType.LITERAL, CommandParser.TokenType.WILDCARD);

        tokens = assertValidTokens("foo * *");
        assertTokensLabel(tokens, "foo", null, null);
        assertTokensType(tokens, CommandParser.TokenType.LITERAL, CommandParser.TokenType.UNNAMED_ARGUMENT, CommandParser.TokenType.WILDCARD);

        tokens = assertValidTokens("foo * bar");
        assertTokensLabel(tokens, "foo", null, "bar");
        assertTokensType(tokens, CommandParser.TokenType.LITERAL, CommandParser.TokenType.WILDCARD, CommandParser.TokenType.LITERAL);

        tokens = assertValidTokens("foo * bar *");
        assertTokensLabel(tokens, "foo", null, "bar", null);
        assertTokensType(tokens, CommandParser.TokenType.LITERAL, CommandParser.TokenType.UNNAMED_ARGUMENT, CommandParser.TokenType.LITERAL, CommandParser.TokenType.WILDCARD);

        tokens = assertValidTokens("foo * * bar *");
        assertTokensLabel(tokens, "foo", null, null, "bar", null);
        assertTokensType(tokens, CommandParser.TokenType.LITERAL, CommandParser.TokenType.UNNAMED_ARGUMENT, CommandParser.TokenType.UNNAMED_ARGUMENT, CommandParser.TokenType.LITERAL, CommandParser.TokenType.WILDCARD);

        tokens = assertValidTokens("foo * * bar {}");
        assertTokensLabel(tokens, "foo", null, null, "bar", null);
        assertTokensType(tokens, CommandParser.TokenType.LITERAL, CommandParser.TokenType.UNNAMED_ARGUMENT, CommandParser.TokenType.WILDCARD, CommandParser.TokenType.LITERAL, CommandParser.TokenType.UNNAMED_ARGUMENT);

        tokens = assertValidTokens("foo * * {}");
        assertTokensLabel(tokens, "foo", null, null, null);
        assertTokensType(tokens, CommandParser.TokenType.LITERAL, CommandParser.TokenType.UNNAMED_ARGUMENT, CommandParser.TokenType.WILDCARD, CommandParser.TokenType.UNNAMED_ARGUMENT);
    }


    private CommandParser.Token[] assertValidTokens(String pattern) {
        return assertDoesNotThrow(() -> parser.tokenize(pattern));
    }
    private static void assertTokensLength(CommandParser.Token[] token, int length) {
        assertEquals(length, token.length);
    }
    private static void assertTokensType(CommandParser.Token[] tokens, CommandParser.TokenType... types) {
        assertEquals(types.length, tokens.length);
        for (int i = 0; i < tokens.length; i++) {
            assertEquals(types[i], tokens[i].type());
        }
    }
    private static void assertTokensLabel(CommandParser.Token[] tokens, String... labels) {
        assertEquals(labels.length, tokens.length);
        for (int i = 0; i < tokens.length; i++) {
            assertEquals(labels[i], tokens[i].label());
        }
    }
}
