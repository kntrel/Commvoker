package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.AssemblyException;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import net.minecraft.commands.arguments.ObjectiveArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.ScoreboardManager;

public class ObjectiveAssembler implements TransformAssembler<CommandSender, String, Objective> {

    private static final ObjectiveAssembler INSTANCE = new ObjectiveAssembler();

    public static ObjectiveAssembler objective() {
        return INSTANCE;
    }


    private ObjectiveAssembler() {}


    @Override
    public Assembler<? super CommandSender, ? extends String> delegate() {
        return Assembler.ofArgumentType(ObjectiveArgument.objective());
    }

    @Override
    public Objective compose(ExecutionContext<? extends CommandSender> ctx, String object) throws AssemblyException {
        ScoreboardManager scoreboardManager = ctx.source().getServer().getScoreboardManager();
        if (scoreboardManager == null) {
            throw new AssemblyException("Scoreboard manager unavailable");
        }

        Objective objective = scoreboardManager.getMainScoreboard().getObjective(object);
        if (objective == null) {
            throw new AssemblyException("Unknown objective " + object, object);
        }
        return objective;
    }
}
