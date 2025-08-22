package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.argument.binding.NameSupplier;
import com.kntrel.mc.commvoker.argument.context.ArgumentContext;
import com.kntrel.mc.commvoker.argument.ArgumentResolver;
import com.kntrel.mc.commvoker.argument.binding.ArgumentDescriptor;
import com.kntrel.mc.commvoker.argument.context.ParameterContext;
import com.kntrel.mc.commvoker.command.*;
import com.kntrel.mc.commvoker.exception.BadCommandMethodException;
import com.kntrel.mc.commvoker.exception.BadCommandTokenException;
import com.kntrel.mc.commvoker.exception.NoSuchArgumentBindingException;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;

class CommandParser<S> {


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

    @SuppressWarnings("unused")
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
        record ParamInfo(Parameter param, int index) {}
        CommandPattern pattern = new CommandPattern(patternTokens);

        // Extracting explicit parameters
        Parameter[] params = method.getParameters();
        ArgumentParser<S>[] argumentParsers = new ArgumentParser[params.length];
        Queue<ParamInfo> explicitParams = new LinkedList<>();
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            try {
                Function<CommandContext<? extends S>, ?> arg = this.argumentResolver_.resolve(new ParameterContext(param, param.getParameterizedType(), method, i));
                argumentParsers[i] = new ArgumentParser<>(arg);
                continue;
            } catch (NoSuchArgumentBindingException ignores) {}

            explicitParams.add(new ParamInfo(param, i));
        }

        //Guard against too few explicit parameters
        if (explicitParams.size() < pattern.argumentCount()) {
            throw new BadCommandMethodException(method,
                "Too few explicit parameters. The command pattern expects a minimum of " + pattern.argumentCount() + " arguments, but the method declares " + explicitParams.size() + " explicit parameters"
            );
        }

        //Building actual command definition
        Map<String, ParamInfo> paramMap = new HashMap<>();
        List<CommandToken> tokens = new ArrayList<>();
        int tokenIndex = 0;
        while (tokenIndex < pattern.size()) {
            CommandPatternToken t = pattern.getTokenAt(tokenIndex);
            if (t.isLiteral()) {
                tokens.add(CommandToken.literal(t.label()));
                tokenIndex++;
                continue;
            }

            if (!t.isWildcard()) {
                tokenIndex++;
            } else if (explicitParams.size() <= pattern.afterWildcardArgumentCount()) {
                tokenIndex++;
                continue;
            }

            ParamInfo paramInfo = explicitParams.poll();
            String label = t.isLabeled() ? t.label() : paramInfo.param().getName();
            if (paramMap.containsKey(label)) {
                throw new BadCommandMethodException(method, "Label '" + label + "' is duplicated");
            }
            tokens.add(CommandToken.argument(label));
            paramMap.put(label, paramInfo);
        }
        CommandDefinition command = new CommandDefinition(tokens);

        //Resolving explicit arguments
        CommandNode<S> head = LiteralArgumentBuilder.<S>literal("___head___").build();
        List<CommandNode<S>> upstream = List.of(head);
        for (int i = 1; i < command.size(); i++) {
            CommandToken t = command.getTokenAt(i);

            Command<S> execution = null;
            if (i >= command.size() - 1) {
                execution = new CommandMethodInvoker<>(instance, method, argumentParsers);
            }

            if (t.isLiteral()) {
                LiteralArgumentBuilder<S> lit = LiteralArgumentBuilder.literal(t.label());
                if (execution != null) {
                    lit.executes(execution);
                }
                CommandNode<S> litNode = lit.build();
                upstream.forEach(n -> n.addChild(litNode));
                upstream = List.of(litNode);
                continue;
            }

            ParamInfo paramInfo = paramMap.get(t.label());
            Parameter param = paramInfo.param();
            ArgumentDescriptor<? super S, ?> descriptor = this.argumentResolver_.resolve(new ArgumentContext(param, param.getParameterizedType(), method, paramInfo.index(), command, i));
            NameSupplier nameSupplier = new NameSupplerImpl(t.label());
            CommandTreeGate<S> gate = (execution == null)
                    ? CommandTemplateCompiler.compile(descriptor.template(), nameSupplier)
                    : CommandTemplateCompiler.compile(descriptor.template(), nameSupplier, execution);
            upstream.forEach(n -> n.addChild(gate.root()));
            upstream = gate.leaves();
            argumentParsers[paramInfo.index()] = new ArgumentParser<>(nameSupplier.namesMap(), descriptor.contextualizer());
        }

        LiteralArgumentBuilder<S> root = LiteralArgumentBuilder.literal(command.getLabelAt(0));
        Iterator<CommandNode<S>> tail = head.getChildren().iterator();
        if (tail.hasNext()) {
            root.then(tail.next());
        } else {
            root.executes(new CommandMethodInvoker<>(instance, method, argumentParsers));
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