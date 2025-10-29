package com.kntrel.mc.commvoker.bukkit.provided.callbacks;

import com.kntrel.mc.commvoker.callback.ReturnCallback;
import com.kntrel.mc.commvoker.command.CommandMethodContext;
import com.mojang.brigadier.Message;
import org.bukkit.command.CommandSender;

public class MessageCallback implements ReturnCallback<CommandSender, Message> {
    @Override
    public void onReturn(CommandMethodContext<? extends CommandSender> context, Message returnValue) {
        context.source().sendMessage(returnValue.getString());
    }
}
