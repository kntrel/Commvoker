package com.kntrel.commvoker.spigot;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.Collections;

public class BukkitCommand extends Command {

    private final CommandDispatcher<CommandSender> delegate_;


    protected BukkitCommand(CommandDispatcher<CommandSender> delegate) {
        super("");
        this.delegate_ = delegate;
    }

    @Override
    public boolean execute(CommandSender commandSender, String label, String[] args) {
        StringBuilder input = new StringBuilder(label);
        for (String a : args) { input.append(" ").append(a); }
        try {
            this.delegate_.execute(input.toString(), commandSender);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}
