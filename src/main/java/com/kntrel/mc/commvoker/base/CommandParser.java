package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.exception.BadCommandMethodException;
import com.kntrel.mc.commvoker.exception.BadCommandTokenException;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class CommandParser {

    //ASSETS
    private static final Pattern VALID_REGEX = Pattern.compile("[A-Za-z0-9._+-]+");
    enum TokenType { LITERAL, ARGUMENT, UNNAMED_ARGUMENT, WILDCARD }
    record Token(int position, String label, TokenType type) {
        @Override public String toString() {
            return switch (this.type()) {
                case LITERAL -> this.label();
                case ARGUMENT -> '<' + this.label() + '>';
                case UNNAMED_ARGUMENT -> "<>";
                case WILDCARD -> "*";
            };
        }

    }


    //FIElDS
    private final ArgumentTypeResolver argumentTypeResolver_;


    //CONSTRUCTORS
    CommandParser(ArgumentTypeResolver argumentTypeResolver) {
        this.argumentTypeResolver_ = argumentTypeResolver;
    }


    //UTIL
    public Token[] tokenize(String raw) throws BadCommandTokenException {
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
                    throw new BadCommandTokenException(StringUtils.normalizeSpace(raw), len + err);
                }
            }

            if (type.equals(TokenType.WILDCARD)) {
                if (lastWildcardPos > 0) {
                    int prevPos = tokens[lastWildcardPos].position();
                    tokens[lastWildcardPos] = new Token(prevPos, null, TokenType.UNNAMED_ARGUMENT);
                }
                lastWildcardPos = i;
            }

            tokens[i] = new Token(len, label, type);
            len += w.length() + 1;
        }

        return tokens;
    }
    public <T> LiteralArgumentBuilder<T> brigadierCommand(String command, Method method) throws BadCommandMethodException {
        try {
            return this.brigadierCommand(this.tokenize(command), method);
        } catch (BadCommandTokenException e) {
            throw new BadCommandMethodException(method, e);
        }
    }
    public <T> LiteralArgumentBuilder<T> brigadierCommand(Token[] tokens, Method  method) throws BadCommandMethodException {
        // Guard assertions
        if (tokens.length == 0) {
            throw new IllegalArgumentException("empty token array");
        }
        if (tokens[0].type() != TokenType.LITERAL) {
            throw new BadCommandMethodException(method, "First token must be a literal");
        }

        // Locating the wildcard and figuring out how many leading and trailing arguments there are
        int wildcardIndex = -1, leadingCount = 0, trailingCount = 0, len = tokens.length;
        for (int i = len - 1; i > 0; i--) {
            Token t = tokens[i];
            if (t.type().equals(TokenType.LITERAL)) { continue; }
            if (wildcardIndex < 0) {
                if (t.type().equals(TokenType.WILDCARD)) {
                    wildcardIndex = i;
                    continue;
                }
                trailingCount++;
            } else {
                leadingCount++;
            }
        }

        // If not explicit wildcard, assume one at the end.
        if (wildcardIndex < 0) {
            wildcardIndex = len;
            len++;
            leadingCount = trailingCount;
            trailingCount = 0;
        }

        // Guard assertion. Ensuring the method has enough parameters to fulfill the token array
        Parameter[] params = method.getParameters();
        int argCount = leadingCount + trailingCount;
        if (argCount > params.length) {
            throw new BadCommandMethodException(method, "Too many arguments. Expected a minimum of " + argCount + ", but method declares " + params.length);
        }
        int middleCount = params.length - argCount;

        LiteralArgumentBuilder<T> root = LiteralArgumentBuilder.literal(tokens[0].label());
        ArgumentBuilder<T, ?> cursor = root;
        int paramIndex = 0;

        for (int i = 1; i < len; i++) {
            if (i == wildcardIndex) {
                while (middleCount-- > 0) {
                    RequiredArgumentBuilder<T, ?> next = buildArg(params[paramIndex++], null, method);
                    cursor.then(next);
                    cursor = next;
                }
                continue;
            }

            Token t = tokens[i];
            if (t.type() == TokenType.LITERAL) {
                cursor = cursor.then(LiteralArgumentBuilder.literal(t.label()));
                continue;
            }

            /* ARGUMENT or UNNAMED_ARGUMENT */
            Parameter p = params[paramIndex++];
            cursor = cursor.then(buildArg(p, t.label(), method));
        }

        return root;
    }

    //HELPERS
    private <T> RequiredArgumentBuilder<T, ?> buildArg(Parameter p, @Nullable String explicitLabel, Method owner) throws BadCommandMethodException {
        ArgumentType<?> at = argumentTypeResolver_.resolve(p.getType(), p).orElseThrow(
            () -> new BadCommandMethodException(owner, "Cannot resolve ArgumentType for parameter " + p.getName())
        );
        String label = (explicitLabel == null || explicitLabel.isEmpty()) ? p.getName() : explicitLabel;
        return RequiredArgumentBuilder.argument(label, at);
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