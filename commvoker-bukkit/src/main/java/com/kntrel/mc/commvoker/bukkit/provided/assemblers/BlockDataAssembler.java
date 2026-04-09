package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import com.kntrel.mc.commvoker.bukkit.internal.CraftBukkitAccessors;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public class BlockDataAssembler implements TransformAssembler<CommandSender, BlockInput, BlockData> {

    private static final BlockDataAssembler INSTANCE = new BlockDataAssembler();

    public static BlockDataAssembler blockData() {
        return INSTANCE;
    }


    private BlockDataAssembler() {}


    @Override
    public Assembler<? super CommandSender, ? extends BlockInput> delegate() {
        return Assembler.ofArgumentType(BlockStateArgument.block(CraftBukkitAccessors.commandBuildContext()));
    }

    @Override
    public BlockData compose(ExecutionContext<? extends CommandSender> ctx, BlockInput object) {
        return CraftBlockData.fromData(object.getState());
    }
}
