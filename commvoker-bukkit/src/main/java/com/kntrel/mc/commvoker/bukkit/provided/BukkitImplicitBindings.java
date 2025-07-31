package com.kntrel.mc.commvoker.bukkit.provided;

import com.kntrel.mc.commvoker.argument.bind.ImplicitArgumentBinding;
import com.kntrel.mc.commvoker.bukkit.annotation.Sender;
import com.kntrel.util.Constants;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Collection;

import static com.kntrel.mc.commvoker.bukkit.provided.SenderPlayerArgument.*;
import static com.kntrel.mc.commvoker.bukkit.provided.SenderArgument.*;
import static com.kntrel.mc.commvoker.bukkit.provided.SenderWorldArgument.*;
import static com.kntrel.mc.commvoker.bukkit.provided.CommandContextArgument.*;
import static com.kntrel.mc.commvoker.argument.bind.ArgumentBinder.*;

public class BukkitImplicitBindings {

    @SuppressWarnings("unchecked")
    public static final ImplicitArgumentBinding<CommandSender, ?>
        PLAYER = implicit(() -> senderPlayer())
            .toClass(Player.class)
            .toAnnotation(Sender.class)
            .requires(s -> s instanceof Player)
            .bind(),

        COMMAND_SENDER = implicit(() -> sender())
            .toClass(CommandSender.class)
            .bind(),

        WORLD = implicit(() -> senderWorld())
            .toClass(World.class)
            .toAnnotation(Sender.class)
            .requires(s -> s instanceof Player)
            .bind(),

        COMMAND_CONTEXT = implicit(() -> commandContext())
            .toClass((Class<CommandContext<CommandSender>>) (Class<?>) CommandContext.class)
            .bind();

    @SuppressWarnings("unchecked")
    public static Collection<ImplicitArgumentBinding<CommandSender, ?>> all() {
        return Constants.getAll(BukkitImplicitBindings.class, (Class<ImplicitArgumentBinding<CommandSender, ?>>) (Class<?>) ImplicitArgumentBinding.class);
    }
}
