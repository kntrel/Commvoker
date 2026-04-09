package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.AssemblyException;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.ColorArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.util.CraftChatMessage;

public class ChatColorAssembler implements TransformAssembler<CommandSender, ChatFormatting, ChatColor> {

    private static final ChatColorAssembler INSTANCE = new ChatColorAssembler();

    public static ChatColorAssembler chatColor() {
        return INSTANCE;
    }


    private ChatColorAssembler() {}


    @Override
    public Assembler<? super CommandSender, ? extends ChatFormatting> delegate() {
        return Assembler.ofArgumentType(ColorArgument.color());
    }

    @Override
    public ChatColor compose(ExecutionContext<? extends CommandSender> ctx, ChatFormatting object) throws AssemblyException {
        ChatColor color = CraftChatMessage.getColor(object);
        if (color == null) {
            throw new AssemblyException("Unsupported chat color " + object.getSerializedName(), object);
        }
        return color;
    }
}
