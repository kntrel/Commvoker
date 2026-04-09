package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.AssemblyException;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import net.minecraft.commands.arguments.TeamArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class TeamAssembler implements TransformAssembler<CommandSender, String, Team> {

    private static final TeamAssembler INSTANCE = new TeamAssembler();

    public static TeamAssembler team() {
        return INSTANCE;
    }


    private TeamAssembler() {}


    @Override
    public Assembler<? super CommandSender, ? extends String> delegate() {
        return Assembler.ofArgumentType(TeamArgument.team());
    }

    @Override
    public Team compose(ExecutionContext<? extends CommandSender> ctx, String object) throws AssemblyException {
        ScoreboardManager scoreboardManager = ctx.source().getServer().getScoreboardManager();
        if (scoreboardManager == null) {
            throw new AssemblyException("Scoreboard manager unavailable");
        }

        Team team = scoreboardManager.getMainScoreboard().getTeam(object);
        if (team == null) {
            throw new AssemblyException("Unknown team " + object, object);
        }
        return team;
    }
}
