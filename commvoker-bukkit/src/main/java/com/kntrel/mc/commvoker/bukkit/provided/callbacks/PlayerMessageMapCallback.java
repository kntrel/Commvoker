package com.kntrel.mc.commvoker.bukkit.provided.callbacks;

import com.kntrel.mc.commvoker.callback.ReturnCallback;
import com.kntrel.mc.commvoker.command.CommandMethodContext;
import com.mojang.brigadier.Message;
import org.bukkit.entity.Player;
import java.util.Map;

public class PlayerMessageMapCallback implements ReturnCallback<Object, Map<Player, Message>> {
    @Override
    public void onReturn(CommandMethodContext<?> context, Map<Player, Message> map) {
        map.forEach((p, m) -> {
            if (p == null || m == null) return;
            String s = m.getString();
            if (s != null && !s.isEmpty()) p.sendMessage(s);
        });
    }
}
