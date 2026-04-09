package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.AssemblyException;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import com.kntrel.mc.commvoker.bukkit.internal.CraftBukkitAccessors;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ItemStackAssembler implements TransformAssembler<CommandSender, ItemInput, ItemStack> {

    private static final ItemStackAssembler INSTANCE = new ItemStackAssembler();

    public static ItemStackAssembler itemStack() {
        return INSTANCE;
    }


    private ItemStackAssembler() {}


    @Override
    public Assembler<? super CommandSender, ? extends ItemInput> delegate() {
        return Assembler.ofArgumentType(ItemArgument.item(CraftBukkitAccessors.commandBuildContext()));
    }

    @Override
    public ItemStack compose(ExecutionContext<? extends CommandSender> ctx, ItemInput object) throws AssemblyException {
        try {
            return CraftItemStack.asBukkitCopy(object.createItemStack(1));
        } catch (CommandSyntaxException e) {
            throw new AssemblyException(e.getMessage());
        }
    }
}
