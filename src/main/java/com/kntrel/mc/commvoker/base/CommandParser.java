package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.exception.BadCommandMethodException;
import com.kntrel.mc.commvoker.exception.BadCommandTokenException;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

class CommandParser<S> {

    //ASSETS
    enum TokenType { LITERAL, ARGUMENT, UNNAMED_ARGUMENT, WILDCARD }
    record Token(String label, TokenType type) {
        @Override public String toString() {
            return switch (this.type()) {
                case LITERAL -> this.label();
                case ARGUMENT -> '<' + this.label() + '>';
                case UNNAMED_ARGUMENT -> "<>";
                case WILDCARD -> "*";
            };
        }
    }
    private record ArgumentTypeInfo(ArgumentType<?> argumentType, String paramName, Class<?> paramType, int arrayIndex) {}
    private static final Token IMPLICIT_WILDCARD = new Token(null, TokenType.WILDCARD);


    //FIElDS
    private final ArgumentTypeResolver<S> argumentTypeResolver_;


    //CONSTRUCTORS
    CommandParser(ArgumentTypeResolver<S> argumentTypeResolver) {
        this.argumentTypeResolver_ = argumentTypeResolver;
    }


    //UTIL
    public Token[] tokenize(String raw) throws BadCommandTokenException {
        if (raw == null || raw.isEmpty()) {
            return new Token[0];
        }

        String[] words = raw.split("\\s+");
        Token[] tokens = new Token[words.length];
        int lastWildcardPos = -1;
        int len = 0;

        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            TokenType type = categorize(w);

            String label = switch (type) {
                case LITERAL -> w;
                case ARGUMENT -> w.substring(1, w.length() - 1);
                default -> null;
            };

            if (label != null) {
                int err = valid(label);
                if (err >= 0) {
                    if (type.equals(TokenType.ARGUMENT)) { err++; }
                    throw new BadCommandTokenException(raw, len + err);
                }
            }

            if (type.equals(TokenType.WILDCARD)) {
                if (lastWildcardPos > 0) {
                    tokens[lastWildcardPos] = new Token(null, TokenType.UNNAMED_ARGUMENT);
                }
                lastWildcardPos = i;
            }

            tokens[i] = new Token(label, type);
            len += w.length() + 1;
        }

        return tokens;
    }
    public LiteralArgumentBuilder<S> brigadierCommand(String command, Method method, Object instance) throws BadCommandMethodException {
        try {
            return this.brigadierCommand(this.tokenize(command), method, instance);
        } catch (BadCommandTokenException e) {
            throw new BadCommandMethodException(method, e);
        }
    }

    @SuppressWarnings("unchecked")
    public LiteralArgumentBuilder<S> brigadierCommand(Token[] tokens, Method method, Object instance) throws BadCommandMethodException {
        // Guard assertions
        if (tokens.length == 0) {
            throw new IllegalArgumentException("empty token array");
        }
        if (tokens[0].type() != TokenType.LITERAL) {
            throw new BadCommandMethodException(method, "First token must be a literal");
        }

        // Locating the wildcard and figuring out how many leading and trailing arguments there are
        int wildcardIndex = -1, leadingCount = 0, trailingCount = 0;
        for (int i = tokens.length - 1; i > 0; i--) {
            Token t = tokens[i];
            if (t.type().equals(TokenType.LITERAL)) { continue; }
            if (t.type().equals(TokenType.WILDCARD)) {
                wildcardIndex = i;
                trailingCount = leadingCount;
                leadingCount = 0;
                continue;
            }
            leadingCount++;
        }
        if (wildcardIndex < 0) {
            wildcardIndex = tokens.length;
            tokens = Arrays.copyOf(tokens, wildcardIndex + 1);
            tokens[wildcardIndex] = IMPLICIT_WILDCARD;
        }

        // Resolving method argument types and collecting parsed ArgumentTypes
        Parameter[] params = method.getParameters();
        ArgumentParser<S, ?>[] argumentParsers = new ArgumentParser[params.length];
        Queue<ArgumentTypeInfo> parsed = new LinkedList<>();
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            ArgumentTypeResolver.Result<S, ?> resolved = this.argumentTypeResolver_.resolve(param.getType(), param, method).orElseThrow(
                () -> new BadCommandMethodException(method, "Cannot resolve ArgumentType for parameter " + param.getName())
            );
            if (resolved.isParsed()) {
                parsed.add(new ArgumentTypeInfo(resolved.parsed(), param.getName(), param.getType(), i));
            } else {
                argumentParsers[i] = ArgumentParser.of(resolved.virtual());
            }
        }

        // Guard against mot enough parsed ArgumentTypes to cover for argument tokens
        int minArgs = leadingCount + trailingCount;
        if (minArgs > parsed.size()) {
            throw new BadCommandMethodException(method, "Command expects a minimum of " + minArgs + " arguments, but methods exposes " + parsed.size());
        }


        // Guard assertion. Ensuring the method has enough parameters to fulfill the token array
        Stack<ArgumentBuilder<S, ?>> stack = new Stack<>();
        stack.add(LiteralArgumentBuilder.literal(tokens[0].label()));
        int i = 1;
        while (i < tokens.length) {
            Token t = tokens[i];

            if (t.type().equals(TokenType.LITERAL)) {
                stack.add(LiteralArgumentBuilder.literal(t.label()));
                i++;
                continue;
            }

            if (i != wildcardIndex) {
                i++;
            } else if (trailingCount >= parsed.size()) {
                i++;
                continue;
            }

            ArgumentTypeInfo argType = parsed.poll();
            if (argType == null) { break; }

            String label = t.label();
            if (label == null || label.isEmpty()) { label = argType.paramName(); }
            stack.add(RequiredArgumentBuilder.argument(label, argType.argumentType()));
            argumentParsers[argType.arrayIndex()] = ArgumentParser.of(label, argType.paramType());
        }

        ArgumentBuilder<S, ?> curr = stack.pop();
        curr.executes(new CommandMethodInvoker<>(instance, method, argumentParsers));
        while (!stack.isEmpty()) {
            ArgumentBuilder<S, ?> prev = curr;
            curr = stack.pop();
            curr.then(prev);
        }

        return (LiteralArgumentBuilder<S>) curr;
    }

    private static TokenType categorize(String word) {
        if (word.equals("*")) {
            return TokenType.WILDCARD;
        }
        if ((word.startsWith("<") && word.endsWith(">")) || (word.startsWith("{") && word.endsWith("}"))) {
            return word.length() > 2 ? TokenType.ARGUMENT : TokenType.UNNAMED_ARGUMENT;
        }
        return TokenType.LITERAL;
    }
    private static int valid(String word) {
        char[] chars = word.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= 'a' && c <= 'z') { continue; }
            if (c >= 'A' && c <= 'Z') { continue; }
            if (c >= '0' && c <= '9') { continue; }
            if (c == '-' || c == '_' || c == '+') { continue; }

            return i;
        }

        return -1;
    }
    private static String commandFromTokens(Token[] tokens) {
        return Arrays.stream(tokens)
                .map(Token::toString)
                .collect(Collectors.joining(" "));
    }
}