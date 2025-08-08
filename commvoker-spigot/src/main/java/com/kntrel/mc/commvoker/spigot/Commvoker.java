package com.kntrel.mc.commvoker.spigot;

import com.kntrel.mc.commvoker.bukkit.BukkitCommvoker;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class Commvoker extends BukkitCommvoker {

    private final CommandMap commandMap_;
    private final BukkitCommand bukkitCommand_;

    public Commvoker(Server server) {
        super(new CommandDispatcherWrapper(BukkitAccessors.commandDispatcher(server)));
        this.commandMap_ = BukkitAccessors.commandMap(server);
        this.bukkitCommand_ = new BukkitCommand(this.getCommandDispatcher());
    }

    public Commvoker(Plugin plugin) {
        this(plugin.getServer());
    }

    @Override public void register(LiteralArgumentBuilder<CommandSender> tree) {
        super.register(tree);
    }


    @Override public CommandDispatcher<CommandSender> getCommandDispatcher() {
        return super.getCommandDispatcher();
    }
}
