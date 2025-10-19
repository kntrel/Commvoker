package com.kntrel.mc.commvoker.bukkit.provided.callbacks;

import com.kntrel.mc.commvoker.bukkit.CommandResult;
import com.kntrel.mc.commvoker.callback.ReturnCallback;
import com.kntrel.mc.commvoker.command.CommandMethodContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import org.bukkit.command.CommandSender;

public class CommandResultCallback implements ReturnCallback<CommandSender, CommandResult> {
    @Override
    public void onReturn(CommandMethodContext<? extends CommandSender> context, CommandResult result) throws CommandSyntaxException {
        if (result.wasSuccessful()) {
            context.source().sendMessage(result.getMessage().getString());
        } else {
            throw new DynamicCommandExceptionType(msg -> result.getMessage()).create(result.getMessage());
        }
    }
}
