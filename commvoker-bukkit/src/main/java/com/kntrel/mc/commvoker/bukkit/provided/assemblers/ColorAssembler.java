package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import net.minecraft.commands.arguments.HexColorArgument;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;

public class ColorAssembler implements TransformAssembler<CommandSender, Integer, Color> {

    private static final ColorAssembler INSTANCE = new ColorAssembler();

    public static ColorAssembler color() {
        return INSTANCE;
    }


    private ColorAssembler() {}


    @Override
    public Assembler<? super CommandSender, ? extends Integer> delegate() {
        return Assembler.ofArgumentType(HexColorArgument.hexColor());
    }

    @Override
    public Color compose(ExecutionContext<? extends CommandSender> ctx, Integer object) {
        return Color.fromRGB(object);
    }
}
