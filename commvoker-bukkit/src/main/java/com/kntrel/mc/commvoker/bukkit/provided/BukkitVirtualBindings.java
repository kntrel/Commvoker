package com.kntrel.mc.commvoker.bukkit.provided;

import com.kntrel.mc.commvoker.argument.bind.VirtualArgumentBinding;
import com.kntrel.mc.commvoker.bukkit.annotation.Sender;
import com.kntrel.util.Constants;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Collection;

import static com.kntrel.mc.commvoker.bukkit.provided.PlayerVirtualArgumentType.*;
import static com.kntrel.mc.commvoker.bukkit.provided.CommandSenderVirtualArgumentType.*;
import static com.kntrel.mc.commvoker.bukkit.provided.WorldVirtualArgumentType.*;
import static com.kntrel.mc.commvoker.argument.bind.ArgumentBinder.*;

public class BukkitVirtualBindings {

    public static final VirtualArgumentBinding<CommandSender, ?>
        PLAYER = virtual(() -> senderPlayer())
            .toClass(Player.class)
            .toAnnotation(Sender.class)
            .requires(s -> s instanceof Player)
            .bind(),
        COMMAND_SENDER = virtual(() -> sender())
            .toClass(CommandSender.class)
            .bind(),
        WORLD = virtual(() -> senderWorld())
            .toClass(World.class)
            .toAnnotation(Sender.class)
            .requires(s -> s instanceof Player)
            .bind();

    @SuppressWarnings("unchecked")
    public static Collection<VirtualArgumentBinding<CommandSender, ?>> all() {
        return Constants.getAll(BukkitVirtualBindings.class, (Class<VirtualArgumentBinding<CommandSender, ?>>) (Class<?>) VirtualArgumentBinding.class);
    }
}
