package com.kntrel.mc.commvoker.bukkit.provided.callbacks;

import com.kntrel.mc.commvoker.callback.ReturnCallback;
import com.kntrel.mc.commvoker.command.CommandMethodContext;
import org.bukkit.command.CommandSender;

public class StringCallBack implements ReturnCallback<CommandSender, String> {

    @Override
    public void onReturn(CommandMethodContext<? extends CommandSender> context, String returnValue) {
        context.source().sendMessage(returnValue);
    }
}
