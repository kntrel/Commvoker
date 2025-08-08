package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.ArgumentContext;
import com.kntrel.mc.commvoker.argument.ArgumentResolver;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.descriptor.ArgumentNode;
import com.kntrel.mc.commvoker.command.CommandPattern;
import com.kntrel.mc.commvoker.command.CommandPatternToken;
import com.kntrel.mc.commvoker.exception.BadCommandMethodException;
import com.kntrel.mc.commvoker.exception.BadCommandTokenException;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.BiFunction;

class CommandParser<S> {

    //ASSETS
    private record ArgInfo<S>(ArgumentDescriptor<? super S, ?> arg, Parameter param, int index) {}


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

        // Extracting explicit parameters
        Parameter[] params = method.getParameters();
        ArgumentParser<S>[] argumentParsers = new ArgumentParser[params.length];
        Queue<ArgInfo<? super S>> explicitArguments = new LinkedList<>();
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            ArgumentDescriptor<? super S, ?> arg = this.argumentResolver_.resolve(new ArgumentContext(param, param.getParameterizedType(), method, i, pattern));
            if (arg.isImplicit()) {
                BiFunction<CommandContext<? extends S>, Object[], ?> contextualizer = (c, o) -> arg.contextualizer().apply(c, o);
                argumentParsers[i] = new ArgumentParser<>(new String[0], contextualizer);
                continue;
            }
            explicitArguments.add(new ArgInfo<>(arg, param, i));
        }

        //Guard against too few explicit parameters
        if (explicitArguments.size() < pattern.argumentCount()) {
            throw new BadCommandMethodException(method,
                "Too few explicit parameters. The command pattern expects a minimum of " + pattern.argumentCount() + " arguments, but the method declares " + explicitArguments.size() + " explicit parameters"
            );
        }

        LiteralArgumentBuilder<S> root = LiteralArgumentBuilder.literal(pattern.getLabelAt(0));
        Deque<ArgumentBuilder<S, ?>> nodes = new LinkedList<>();
        nodes.add(root);

        int tokenIndex = 1;
        while (tokenIndex < pattern.size()) {
            CommandPatternToken t = pattern.getTokenAt(tokenIndex);
            if (t.isLiteral()) {
                nodes.add(LiteralArgumentBuilder.literal(t.label()));
                tokenIndex++;
                continue;
            }

            if (!t.isWildcard()) {
                tokenIndex++;
            } else if (explicitArguments.size() <= pattern.afterWildcardArgumentCount()) {
                tokenIndex++;
                continue;
            }

            ArgInfo<? super S> argInfo = explicitArguments.poll();
            ArgumentDescriptor<? super S, ?> arg = argInfo.arg();
            String label = t.isLabeled() ? t.label() : argInfo.param().getName();
            Collection<? extends ArgumentNode<? super S, ?>> argumentNodes = arg.argumentNodes();
            List<String> names = new ArrayList<>(argumentNodes.size());
            for (ArgumentNode<? super S, ?> n : argumentNodes) {
                String l = argumentNodes.size() < 2 ? label : label + names.size();
                RequiredArgumentBuilder<S, ?> builder = RequiredArgumentBuilder.argument(l, n.argumentType());
                if (n.suggestionProvider() != null) {
                    builder.suggests((SuggestionProvider<S>) n.suggestionProvider());
                }
                names.add(l);
                nodes.add(builder);
            }
            argumentParsers[argInfo.index()] = new ArgumentParser<S>(names.toArray(new String[0]), (BiFunction) arg.contextualizer());

        }

        ArgumentBuilder<S, ?> curr = nodes.pollLast();
        curr.executes(new CommandMethodInvoker<>(instance, method, argumentParsers));
        while (!nodes.isEmpty()) {
            ArgumentBuilder<S, ?> next = nodes.pollLast();
            next.then(curr);
            curr = next;
        }

        return root;
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