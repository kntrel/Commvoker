package com.kntrel.commvoker.spigot;

import com.mojang.brigadier.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.command.CommandSender;

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

    public void executeDelegate(String command, CommandSender source) throws CommandSyntaxException {
        this.delegate_.execute(command, Adapters.commandSender(source));
    }

}
