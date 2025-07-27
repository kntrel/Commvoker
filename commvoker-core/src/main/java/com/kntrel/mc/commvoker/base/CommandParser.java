package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.mc.commvoker.argument.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.ArgumentResolver;
import com.kntrel.mc.commvoker.argument.ParameterContext;
import com.kntrel.mc.commvoker.command.CommandDefinition;
import com.kntrel.mc.commvoker.command.CommandPattern;
import com.kntrel.mc.commvoker.command.CommandPatternToken;
import com.kntrel.mc.commvoker.command.CommandToken;
import com.kntrel.mc.commvoker.exception.BadCommandMethodException;
import com.kntrel.mc.commvoker.exception.BadCommandTokenException;
import com.kntrel.mc.commvoker.exception.NoSuchArgumentBindingException;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Predicate;

class CommandParser<S> {

    //ASSETS
    private record ParamInfo(Parameter param, int index) {}


    //FIElDS
    private final ArgumentResolver<S> argumentResolver_;


    //CONSTRUCTORS
    CommandParser(ArgumentResolver<S> argumentTypeResolver) {
        this.argumentResolver_ = argumentTypeResolver;
    }


    //UTIL
    public CommandPatternToken[] tokenize(String raw) throws BadCommandTokenException {
        if (raw == null || raw.isEmpty()) {
            return new CommandPatternToken[0];
        }

        String[] words = raw.split("\\s+");
        CommandPatternToken[] tokens = new CommandPatternToken[words.length];
        int lastWildcardPos = -1;
        int len = 0;

        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            CommandPatternToken.Type type = categorize(w);

            String label = switch (type) {
                case LITERAL -> w;
                case ARGUMENT -> w.substring(1, w.length() - 1);
                default -> null;
            };

            if (label != null) {
                int err = valid(label);
                if (err >= 0) {
                    if (type.equals(CommandPatternToken.Type.ARGUMENT)) { err++; }
                    throw new BadCommandTokenException(raw, len + err);
                }
            }

            if (type.equals(CommandPatternToken.Type.WILDCARD)) {
                if (lastWildcardPos > 0) {
                    tokens[lastWildcardPos] = CommandPatternToken.argument();
                }
                lastWildcardPos = i;
            }

            tokens[i] = switch (type) {
                case LITERAL -> CommandPatternToken.literal(label);
                case ARGUMENT -> CommandPatternToken.argument(label);
                case UNNAMED_ARGUMENT -> CommandPatternToken.argument();
                case WILDCARD -> CommandPatternToken.wildcard();
            };
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
    public LiteralArgumentBuilder<S> brigadierCommand(CommandPatternToken[] patternTokens, Method method, Object instance) throws BadCommandMethodException {
        // Guard assertions
        if (patternTokens.length == 0) {
            throw new IllegalArgumentException("empty CommandToken array");
        }
        if (patternTokens[0].type() != CommandPatternToken.Type.LITERAL) {
            throw new BadCommandMethodException(method, "First CommandToken must be a literal");
        }
        CommandPattern pattern = new CommandPattern(patternTokens);

        // Splitting virtual-mapped and parsed-mapped parameters
        Parameter[] params = method.getParameters();
        ArgumentParser<S, ?>[] argumentParsers = new ArgumentParser[params.length];
        Queue<ParamInfo> parsedParams = new LinkedList<>();
        List<Predicate<S>> requirements = new LinkedList<>();
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            ParameterContext ctx = new ParameterContext(param, param.getParameterizedType(), method, i);
            try {
                ArgumentDescriptor.Virtual<S, ?> desc = this.argumentResolver_.resolveVirtual(ctx);
                argumentParsers[i] = ArgumentParser.of(desc.argumentType());
                Predicate<S> req = desc.requirement();
                if (req != null) { requirements.add(req); }
            } catch (NoSuchArgumentBindingException ignored) {
                parsedParams.add(new ParamInfo(param, i));
            }
        }

        // Guard against mot enough parsed ArgumentTypes to cover for argument tokens
        if (pattern.argumentCount() > parsedParams.size()) {
            throw new BadCommandMethodException(method, "Command expects a minimum of " + pattern.argumentCount() + " arguments, but methods exposes " + parsedParams.size());
        }

        // Compiling a concrete CommandDefinition from the CommandPattern and parsed parameters
        List<CommandToken> tokens = new LinkedList<>();
        tokens.add(new CommandToken(pattern.getLabelAt(0), CommandToken.Type.LITERAL));
        Map<Integer, ParamInfo> paramTokens = new HashMap<>();
        int i = 1;
        while (i < pattern.size()) {
            CommandPatternToken t = pattern.getTokenAt(i);

            if (t.isLiteral()) {
                tokens.add(new CommandToken(t.label(), CommandToken.Type.LITERAL));
                i++;
                continue;
            }

            if (!t.isWildcard()) {
                i++;
            } else if (pattern.afterWildcardArgumentCount() >= parsedParams.size()) {
                i++;
                continue;
            }

            ParamInfo paramInfo = parsedParams.poll();
            if (paramInfo == null) { break; }

            String label = t.label();
            if (label == null || label.isEmpty()) { label = paramInfo.param().getName(); }
            CommandToken token = new CommandToken(label, CommandToken.Type.ARGUMENT);
            paramTokens.put(tokens.size(), paramInfo);
            tokens.add(token);
        }
        CommandDefinition command = new CommandDefinition(tokens);

        // Building the brigadier tree
        Deque<ArgumentBuilder<S, ?>> stack = new LinkedList<>();
        List<ArgumentType<?>> resolvedTypes = new LinkedList<>();
        for (i = 0; i < command.size(); i++) {
            CommandToken t = command.getTokenAt(i);
            if (t.isLiteral()) {
                stack.add(LiteralArgumentBuilder.literal(t.label()));
                continue;
            }

            ParamInfo paramInfo = paramTokens.get(i);
            Parameter param = paramInfo.param();
            ArgumentContext ctx = new ArgumentContext(param, param.getParameterizedType(), method, paramInfo.index(), command, i, resolvedTypes.toArray(new ArgumentType[0]));
            ArgumentDescriptor.Parsed<S, ?> desc = this.argumentResolver_.resolve(ctx);
            ArgumentType<?> argType = desc.argumentType();;

            RequiredArgumentBuilder<S, ?> arg = RequiredArgumentBuilder.argument(t.label(), argType);
            if (desc.requirement() != null) { arg.requires(desc.requirement()); }
            stack.add(arg);
            resolvedTypes.add(argType);
            argumentParsers[paramInfo.index()] = ArgumentParser.of(t.label(), param.getType());
        }

        // Connecting the brigadier tree (must be done backwards)
        ArgumentBuilder<S, ?> curr = stack.pollLast();
        curr.executes(new CommandMethodInvoker<>(instance, method, argumentParsers));
        while (!stack.isEmpty()) {
            ArgumentBuilder<S, ?> prev = curr;
            curr = stack.pollLast();
            curr.then(prev);
        }

        return (LiteralArgumentBuilder<S>) curr;
    }

    private static CommandPatternToken.Type categorize(String word) {
        if (word.equals("*")) {
            return CommandPatternToken.Type.WILDCARD;
        }
        if ((word.startsWith("<") && word.endsWith(">")) || (word.startsWith("{") && word.endsWith("}"))) {
            return word.length() > 2 ? CommandPatternToken.Type.ARGUMENT : CommandPatternToken.Type.UNNAMED_ARGUMENT;
        }
        return CommandPatternToken.Type.LITERAL;
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
}