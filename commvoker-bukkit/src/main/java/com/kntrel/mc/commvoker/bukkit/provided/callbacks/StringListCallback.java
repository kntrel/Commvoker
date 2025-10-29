package com.kntrel.mc.commvoker.bukkit.provided.callbacks;

import com.kntrel.mc.commvoker.callback.ReturnCallback;
import com.kntrel.mc.commvoker.command.CommandMethodContext;
import org.bukkit.command.CommandSender;
import java.util.List;

public class StringListCallback implements ReturnCallback<CommandSender, List<String>> {

    @Override
    public void onReturn(CommandMethodContext<? extends CommandSender> context, List<String> returnValue) {
        returnValue.forEach(context.source()::sendMessage);
    }
}
