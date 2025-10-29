package com.kntrel.mc.commvoker.bukkit.provided.callbacks;

import com.kntrel.mc.commvoker.callback.ReturnCallback;
import com.kntrel.mc.commvoker.command.CommandMethodContext;
import org.bukkit.entity.Player;

import java.util.Map;

public class PlayerStringMapCallback implements ReturnCallback<Object, Map<Player, String>> {
    @Override
    public void onReturn(CommandMethodContext<?> context, Map<Player, String> map) {
        map.forEach((p, s) -> {
            if (s != null && !s.isEmpty()) p.sendMessage(s);
        });
    }
}
