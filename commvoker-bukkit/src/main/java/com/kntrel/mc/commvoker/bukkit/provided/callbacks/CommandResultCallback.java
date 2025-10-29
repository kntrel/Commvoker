package com.kntrel.mc.commvoker.bukkit.provided.callbacks;

import com.kntrel.mc.commvoker.bukkit.CommandResult;
import com.kntrel.mc.commvoker.callback.ReturnCallback;
import com.kntrel.mc.commvoker.command.CommandMethodContext;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import org.bukkit.command.CommandSender;

public class CommandResultCallback implements ReturnCallback<CommandSender, CommandResult> {
    @Override
    public void onReturn(CommandMethodContext<? extends CommandSender> context, CommandResult result) throws CommandSyntaxException {
        result.getPlayerMessages().forEach((p, m) -> {
            if (p == null || m == null) return;
            String s = m.getString();
            if (s != null && !s.isEmpty()) p.sendMessage(s);
        });

        if (result.wasSuccessful()) {
            Message m = result.getMessage();
            if (m == null) { return; }
            context.source().sendMessage(m.getString());
        } else {
            throw new DynamicCommandExceptionType(msg -> result.getMessage()).create(result.getMessage());
        }
    }
}