package com.kntrel.mc.commvoker.bukkit.provided.assemblers;

import com.kntrel.mc.commvoker.argument.context.ExecutionContext;
import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.TransformAssembler;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Criteria;

public class CriteriaAssembler implements TransformAssembler<CommandSender, ObjectiveCriteria, Criteria> {

    private static final CriteriaAssembler INSTANCE = new CriteriaAssembler();

    public static CriteriaAssembler criteria() {
        return INSTANCE;
    }


    private CriteriaAssembler() {}


    @Override
    public Assembler<? super CommandSender, ? extends ObjectiveCriteria> delegate() {
        return Assembler.ofArgumentType(ObjectiveCriteriaArgument.criteria());
    }

    @Override
    public Criteria compose(ExecutionContext<? extends CommandSender> ctx, ObjectiveCriteria object) {
        return Criteria.create(object.getName());
    }
}
