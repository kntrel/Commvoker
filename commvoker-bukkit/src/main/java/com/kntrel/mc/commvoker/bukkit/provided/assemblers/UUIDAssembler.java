package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.assembler.ArgumentTypeAssembler;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.arguments.UuidArgument;
import java.util.UUID;

public class UUIDAssembler implements ArgumentTypeAssembler<UUID> {

    private static final UUIDAssembler INSTANCE = new UUIDAssembler();

    public static UUIDAssembler uuid() {
        return INSTANCE;
    }


    private UUIDAssembler() {}


    @Override
    public ArgumentType<? extends UUID> argumentType() {
        return UuidArgument.uuid();
    }
}
