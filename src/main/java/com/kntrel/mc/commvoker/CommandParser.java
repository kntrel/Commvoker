package com.kntrel.mc.commvoker;

import com.kntrel.mc.commvoker.annotation.Command;
import com.kntrel.mc.commvoker.argument.ArgumentTypeResolver;
import com.kntrel.mc.commvoker.exception.BadCommandMethodException;
import com.mojang.brigadier.arguments.ArgumentType;
import org.apache.commons.lang3.StringUtils;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class CommandParser {

    private CommandParser() {}

    public sealed interface Token permits Literal, Argument {
        String label();
    }
    public record Literal(String label) implements Token {}
    public record Argument(String label, ArgumentType<?> argumentType) implements Token {}


    private enum Part { LITERAL, PLACEHOLDER, WILDCARD }
    private static final Pattern LITERAL_RE = Pattern.compile("[A-Za-z0-9._+-]+");


    public static Token[] parse(ArgumentTypeResolver resolver, String raw, Method method) throws BadCommandMethodException {

        raw = StringUtils.normalizeSpace(raw);
        Command annotation = method.getAnnotation(Command.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Non @Command annotated method");
        }

        Parameter[] params = method.getParameters();
        int paramIndex = 0;
        int wildcardPos = -1;
        List<Token> out = new ArrayList<>();

        for (String part : raw.split("\\s+")) {
            Part kind = classify(part);

            switch (kind) {
                case LITERAL -> {
                    ensureValidLiteral(part, method, raw);
                    out.add(new Literal(part));
                }

                case PLACEHOLDER, WILDCARD -> {
                    if (paramIndex >= params.length) {
                        throw new BadCommandMethodException(method, raw, raw.indexOf(part),
                            "Too many placeholders for method parameters."
                        );
                    }
                    Parameter p = params[paramIndex++];
                    Class<?> paramType = p.getType();
                    ArgumentType<?> argTyp = resolver.resolve(paramType, p).orElseThrow(() -> new IllegalStateException(
                        "No ArgumentType for " + p.getType().getName()
                    ));

                    String label = kind == Part.PLACEHOLDER && part.length() > 2
                            ? part.substring(1, part.length() - 1)
                            : p.getName();

                    if (kind == Part.WILDCARD) {
                        wildcardPos = out.size();
                    }
                    out.add(new Argument(label, argTyp));
                }
            }
        }

        /* ── Append any parameters that were not matched in the loop ── */
        if (paramIndex < params.length) {
            if (wildcardPos < 0) wildcardPos = out.size();
            for (; paramIndex < params.length; paramIndex++) {
                Parameter p = params[paramIndex];
                Class<?> paramType = p.getType();
                ArgumentType<?> argTyp  = resolver.resolve(paramType, p).orElseThrow(() -> new IllegalStateException(
                    "No ArgumentType for " + p.getType().getName()
                ));
                out.add(wildcardPos++, new Argument(p.getName(), argTyp));
            }
        }
        return out.toArray(Token[]::new);
    }

    public static boolean checkValidLiteral(String s) {
        return LITERAL_RE.matcher(s).matches();
    }


    private static Part classify(String part) {
        if (part.equals("*")) { return Part.WILDCARD; }
        if (part.startsWith("{") && part.endsWith("}")) { return Part.PLACEHOLDER; }
        return Part.LITERAL;
    }



    private static void ensureValidLiteral(String lit, Method m, String cmd) throws BadCommandMethodException {

        if (!checkValidLiteral(lit)) {
            int pos = cmd.indexOf(lit);
            throw new BadCommandMethodException(
                    m, cmd, pos, "Invalid character in literal: " + lit);
        }
    }
}