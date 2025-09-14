package com.kntrel.mc.commvoker.bukkit.requirement;

import com.kntrel.mc.commvoker.requirement.AnnotatedRequirement;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class PlayerRequirement implements AnnotatedRequirement<CommandSender, RequiresPlayer> {
    @Override
    public boolean test(CommandSender source, RequiresPlayer annotation) {
        return source instanceof Player;
    }
}
