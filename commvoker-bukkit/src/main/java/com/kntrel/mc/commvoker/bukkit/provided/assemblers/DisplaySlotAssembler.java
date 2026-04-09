package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.DisplaySlot;

public class DisplaySlotAssembler implements TransformAssembler<CommandSender, net.minecraft.world.scores.DisplaySlot, DisplaySlot> {

    private static final DisplaySlotAssembler INSTANCE = new DisplaySlotAssembler();

    public static DisplaySlotAssembler displaySlot() {
        return INSTANCE;
    }


    private DisplaySlotAssembler() {}


    @Override
    public Assembler<? super CommandSender, ? extends net.minecraft.world.scores.DisplaySlot> delegate() {
        return Assembler.ofArgumentType(ScoreboardSlotArgument.displaySlot());
    }

    @Override
    public DisplaySlot compose(ExecutionContext<? extends CommandSender> ctx, net.minecraft.world.scores.DisplaySlot object) {
        return switch (object) {
            case LIST -> DisplaySlot.PLAYER_LIST;
            case SIDEBAR -> DisplaySlot.SIDEBAR;
            case BELOW_NAME -> DisplaySlot.BELOW_NAME;
            case TEAM_BLACK -> DisplaySlot.SIDEBAR_BLACK;
            case TEAM_DARK_BLUE -> DisplaySlot.SIDEBAR_DARK_BLUE;
            case TEAM_DARK_GREEN -> DisplaySlot.SIDEBAR_DARK_GREEN;
            case TEAM_DARK_AQUA -> DisplaySlot.SIDEBAR_DARK_AQUA;
            case TEAM_DARK_RED -> DisplaySlot.SIDEBAR_DARK_RED;
            case TEAM_DARK_PURPLE -> DisplaySlot.SIDEBAR_DARK_PURPLE;
            case TEAM_GOLD -> DisplaySlot.SIDEBAR_GOLD;
            case TEAM_GRAY -> DisplaySlot.SIDEBAR_GRAY;
            case TEAM_DARK_GRAY -> DisplaySlot.SIDEBAR_DARK_GRAY;
            case TEAM_BLUE -> DisplaySlot.SIDEBAR_BLUE;
            case TEAM_GREEN -> DisplaySlot.SIDEBAR_GREEN;
            case TEAM_AQUA -> DisplaySlot.SIDEBAR_AQUA;
            case TEAM_RED -> DisplaySlot.SIDEBAR_RED;
            case TEAM_LIGHT_PURPLE -> DisplaySlot.SIDEBAR_LIGHT_PURPLE;
            case TEAM_YELLOW -> DisplaySlot.SIDEBAR_YELLOW;
            case TEAM_WHITE -> DisplaySlot.SIDEBAR_WHITE;
        };
    }
}
