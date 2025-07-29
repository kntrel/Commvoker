package com.kntrel.commvoker.spigot;

import com.mojang.brigadier.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;
import java.util.concurrent.CompletableFuture;

class CommandDispatcherWrapper extends CommandDispatcher<CommandSender> {

    private final CommandDispatcher<CommandSourceStack> delegate_;


    CommandDispatcherWrapper(CommandDispatcher<CommandSourceStack> delegate) {
        this.delegate_ = delegate;
    }


    @Override
    public LiteralCommandNode<CommandSender> register(LiteralArgumentBuilder<CommandSender> command) {
        this.delegate_.register(Adapters.literalCommandArgument(command, this.getRoot() ));
        return super.register(command);
    }

    @Override
    public void setConsumer(ResultConsumer<CommandSender> consumer) {
        this.delegate_.setConsumer(new Adapters.AdaptedResultConsumer(consumer, this.getRoot()));
        super.setConsumer(consumer);
    }


    @Override
    public int execute(StringReader input, CommandSender source) throws CommandSyntaxException {
        return this.delegate_.execute(input, Adapters.commandSender(source));
    }

    @Override
    public int execute(ParseResults<CommandSender> parse) throws CommandSyntaxException {
        return this.delegate_.execute(parse.getReader().getString(), Adapters.commandSender(parse.getContext().getSource()));
    }

    @Override
    public CompletableFuture<Suggestions> getCompletionSuggestions(ParseResults<CommandSender> parse) {
        return super.getCompletionSuggestions(parse);
    }

    @Override
    public CompletableFuture<Suggestions> getCompletionSuggestions(ParseResults<CommandSender> parse, int cursor) {
        return super.getCompletionSuggestions(parse, cursor);
    }

    @Override
    public void findAmbiguities(AmbiguityConsumer<CommandSender> consumer) {
        super.findAmbiguities(consumer);
    }

}
