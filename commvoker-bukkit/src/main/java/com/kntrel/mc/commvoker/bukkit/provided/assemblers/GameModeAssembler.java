package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.AssemblyException;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.world.level.GameType;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;

public class GameModeAssembler implements TransformAssembler<CommandSender, GameType, GameMode> {

    private static final GameModeAssembler INSTANCE = new GameModeAssembler();

    public static GameModeAssembler gameMode() {
        return INSTANCE;
    }


    private GameModeAssembler() {}


    @Override
    public Assembler<? super CommandSender, ? extends GameType> delegate() {
        return Assembler.ofArgumentType(GameModeArgument.gameMode());
    }

    @Override
    public GameMode compose(ExecutionContext<? extends CommandSender> ctx, GameType object) throws AssemblyException {
        GameMode gameMode = GameMode.getByValue(object.getId());
        if (gameMode == null) {
            throw new AssemblyException("Unsupported game mode " + object.getSerializedName(), object);
        }
        return gameMode;
    }
}
