package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import net.minecraft.commands.arguments.HeightmapTypeArgument;
import org.bukkit.HeightMap;
import org.bukkit.command.CommandSender;

public class HeightMapAssembler implements TransformAssembler<CommandSender, net.minecraft.world.level.levelgen.Heightmap.Types, HeightMap> {

    private static final HeightMapAssembler INSTANCE = new HeightMapAssembler();

    public static HeightMapAssembler heightMap() {
        return INSTANCE;
    }


    private HeightMapAssembler() {}


    @Override
    public Assembler<? super CommandSender, ? extends net.minecraft.world.level.levelgen.Heightmap.Types> delegate() {
        return Assembler.ofArgumentType(HeightmapTypeArgument.heightmap());
    }

    @Override
    public HeightMap compose(ExecutionContext<? extends CommandSender> ctx, net.minecraft.world.level.levelgen.Heightmap.Types object) {
        return HeightMap.valueOf(object.name());
    }
}
