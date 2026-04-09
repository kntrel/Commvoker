package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.AssemblyException;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.resources.Identifier;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

public class WorldAssembler implements TransformAssembler<CommandSender, Identifier, World> {

    private static final WorldAssembler INSTANCE = new WorldAssembler();

    //FACTORY
    public static WorldAssembler world() {
        return INSTANCE;
    }



    //CONSTRUCTOR
    private WorldAssembler() {}


    //IMPLEMENTATION
    @Override
    public Assembler<? super CommandSender, ? extends Identifier> delegate() {
        return Assembler.ofArgumentType(DimensionArgument.dimension());
    }
    @Override
    public World compose(ExecutionContext<? extends CommandSender> ctx, Identifier dimensionId) throws AssemblyException {
        NamespacedKey key = CraftNamespacedKey.fromMinecraft(dimensionId);
        return ctx.source().getServer().getWorlds().stream()
                .filter(world -> world.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new AssemblyException("Unknown world " + key, dimensionId));
    }
}
