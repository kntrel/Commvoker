package com.kntrel.mc.commvoker.bukkit.requirement;

import com.kntrel.mc.commvoker.requirement.AnnotatedRequirement;
import org.bukkit.command.CommandSender;
import java.util.Arrays;

class PermissionRequirement implements AnnotatedRequirement<CommandSender, RequiresPermission> {
    @Override
    public boolean test(CommandSender source, RequiresPermission annotation) {
        return Arrays.stream(annotation.value()).allMatch(source::hasPermission);
    }
}
