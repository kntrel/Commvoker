package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.AssemblerHook;
import com.kntrel.mc.commvoker.assembler.ComposedAssembler;
import com.kntrel.mc.commvoker.bukkit.internal.CraftBukkitAccessors;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import org.bukkit.command.CommandSender;
import org.bukkit.util.BlockVector;

public class BlockVectorAssembler implements ComposedAssembler<CommandSender, BlockVector> {

    private static final BlockVectorAssembler INSTANCE = new BlockVectorAssembler();

    public static BlockVectorAssembler blockVector() {
        return INSTANCE;
    }


    private BlockVectorAssembler() {}


    @Override
    public void composedOf(AssemblerHook<CommandSender> hook) {
        hook.hook("blockPosArg", Assembler.ofArgumentType(BlockPosArgument.blockPos()));
    }

    @Override
    public BlockVector assemble(ExecutionContext<? extends CommandSender> ctx) {
        Coordinates coordinates = ctx.component("blockPosArg", Coordinates.class);
        BlockPos pos = coordinates.getBlockPos(CraftBukkitAccessors.commandSourceStack(ctx.source()));
        return new BlockVector(pos.getX(), pos.getY(), pos.getZ());
    }
}
