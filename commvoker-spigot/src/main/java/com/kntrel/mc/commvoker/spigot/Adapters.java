package com.kntrel.mc.commvoker.spigot;

import com.kntrel.mc.commvoker.bukkit.internal.CraftBukkitAccessors;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

class Adapters {

    private static final Field COMMAND_CONTEXT_ARGUMENT_FIELD;
    static {
        try {
            COMMAND_CONTEXT_ARGUMENT_FIELD = CommandContext.class.getDeclaredField("arguments");
            COMMAND_CONTEXT_ARGUMENT_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static CommandContext<CommandSender> commandContext(CommandContext<CommandSourceStack> ctx, Command<CommandSender> command, CommandNode<CommandSender> rootNode) {
        Map<String, ParsedArgument<CommandSourceStack, ?>> arguments;
        try {
            arguments = (Map<String, ParsedArgument<CommandSourceStack, ?>>) COMMAND_CONTEXT_ARGUMENT_FIELD.get(ctx);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        Map<String, ParsedArgument<CommandSender, ?>> argumentsCopy = new HashMap<>();
        arguments.forEach((s, a) -> argumentsCopy.put(s, parsedArgument(a)));

        return new CommandContext<>(
                ctx.getSource().getBukkitSender(),
                ctx.getInput(),
                argumentsCopy,
                command,
                rootNode,
                nodeList(ctx.getNodes(), rootNode),
                ctx.getRange(),
                (ctx.getChild() != null) ? commandContext(ctx.getChild(), command, rootNode) : null,
                (ctx.getRedirectModifier() != null) ? redirectModifier(ctx.getRedirectModifier(), ctx) : null,
                ctx.isForked()
        );
    }


    static ParsedArgument<CommandSender, ?> parsedArgument(ParsedArgument<CommandSourceStack, ?> argument) {
        StringRange range = argument.getRange();
        return new ParsedArgument<>(range.getStart(), range.getEnd(), argument.getResult());
    }

    static List<ParsedCommandNode<CommandSender>> nodeList(Collection<ParsedCommandNode<CommandSourceStack>> guide, CommandNode<CommandSender> root) {
        List<ParsedCommandNode<CommandSender>> out = new LinkedList<>();

        CommandNode<CommandSender> curr = root;
        for (ParsedCommandNode<CommandSourceStack> n : guide) {
            CommandNode<CommandSender> next = curr.getChild(n.getNode().getName());
            if (next == null) { break; }

            out.add(new ParsedCommandNode<>(next, n.getRange()));
            curr = next;
        }

        return out;
    }

    static CommandSourceStack commandSender(CommandSender sender) {
        return (CommandSourceStack) CraftBukkitAccessors.getCommandSourceStack(sender);
    }

    static RedirectModifier<CommandSender> redirectModifier(RedirectModifier<CommandSourceStack> redirectModifier, CommandContext<CommandSourceStack> context) {
        return new AdaptedRedirectModifier(redirectModifier, context);
    }

    static Command<CommandSourceStack> command(Command<CommandSender> command, RootCommandNode<CommandSender> rootNode) {
        return new AdaptedCommand(command, rootNode);
    }

    static LiteralArgumentBuilder<CommandSourceStack> literalCommandArgument(LiteralArgumentBuilder<CommandSender> src, RootCommandNode<CommandSender> rootNode) {

        LiteralArgumentBuilder<CommandSourceStack> copy = LiteralArgumentBuilder.literal(src.getLiteral());

        final Predicate<CommandSender> requirement = src.getRequirement();
        if (requirement != null) {
            copy.requires(s -> requirement.test(s.getBukkitSender()));
        }

        final Command<CommandSender> command = src.getCommand();
        if (command != null) {
            copy.executes(command(command, rootNode));
        }

        for (CommandNode<CommandSender> child : src.getArguments()) {
            CommandNode<CommandSourceStack> childCopy = commandNode(child, rootNode);
            copy.then(childCopy);
        }

        return copy;
    }

    static CommandNode<CommandSourceStack> commandNode(CommandNode<CommandSender> src, RootCommandNode<CommandSender> rootNode) {

        ArgumentBuilder<CommandSourceStack, ?> copy;
        if (src instanceof ArgumentCommandNode<CommandSender,?> arg) {
            RequiredArgumentBuilder<CommandSourceStack, ?> argCopy = RequiredArgumentBuilder.argument(arg.getName(), arg.getType());
            final SuggestionProvider<CommandSender> suggestion = arg.getCustomSuggestions();
            if (suggestion != null) {
                argCopy.suggests((ctx, b) -> suggestion.getSuggestions(commandContext(ctx, null, rootNode), b));
            }
            copy = argCopy;
        } else {
            LiteralCommandNode<CommandSender> lit = (LiteralCommandNode<CommandSender>) src;
            copy = LiteralArgumentBuilder.literal(lit.getLiteral());
        }

        final Predicate<CommandSender> requirement = src.getRequirement();
        if (requirement != null) {
            copy.requires(s -> requirement.test(s.getBukkitSender()));
        }

        final Command<CommandSender> command = src.getCommand();
        if (command != null) {
            copy.executes(command(command, rootNode));
        }

        for (CommandNode<CommandSender> child : src.getChildren()) {
            CommandNode<CommandSourceStack> childCopy = commandNode(child, rootNode);
            copy.then(childCopy);
        }

        return copy.build();
    }



    // PRIVATE CLASS ADAPTERS
    private static class AdaptedRedirectModifier implements RedirectModifier<CommandSender> {

        private final RedirectModifier<CommandSourceStack> delegate_;
        private final CommandContext<CommandSourceStack> context_;

        public AdaptedRedirectModifier(RedirectModifier<CommandSourceStack> delegate, CommandContext<CommandSourceStack> context) {
            this.delegate_ = delegate;
            this.context_ = context;
        }

        @Override
        public Collection<CommandSender> apply(CommandContext<CommandSender> commandContext) throws CommandSyntaxException {
            Collection<CommandSourceStack> original = this.delegate_.apply(this.context_);
            return original.stream().map(CommandSourceStack::getBukkitSender).toList();
        }
    }

    static class AdaptedCommand implements Command<CommandSourceStack> {

        private final Command<CommandSender> delegate_;
        private final RootCommandNode<CommandSender> rootNode_;

        AdaptedCommand(Command<CommandSender> delegate, RootCommandNode<CommandSender> rootNode) {
            this.delegate_ = delegate;
            this.rootNode_ = rootNode;
        }

        @Override
        public int run(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
            return this.delegate_.run(commandContext(ctx, this.delegate_, this.rootNode_));
        }
    }

    static class AdaptedResultConsumer implements ResultConsumer<CommandSourceStack> {

        private final ResultConsumer<CommandSender> delegate_;
        private final RootCommandNode<CommandSender> rootNode_;

        AdaptedResultConsumer(ResultConsumer<CommandSender> delegate, RootCommandNode<CommandSender> rootNode) {
            this.delegate_ = delegate;
            this.rootNode_ = rootNode;
        }

        @Override
        public void onCommandComplete(CommandContext<CommandSourceStack> ctx, boolean b, int i) {
            CommandContext<CommandSender> wrapper = commandContext(
                    ctx,
                    (ctx.getCommand() instanceof AdaptedCommand a) ? a.delegate_ : null,
                    this.rootNode_
            );
            this.delegate_.onCommandComplete(wrapper, b, i);
        }
    }
}
