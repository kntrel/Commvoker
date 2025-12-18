package com.kntrel.mc.commvoker.provided.exceptionHandler;

import com.kntrel.mc.commvoker.argument.Component;
import com.kntrel.mc.commvoker.assembler.AssemblerException;
import com.kntrel.mc.commvoker.error.CommandExceptionHandler;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

public class AssemblerExceptionHandler implements CommandExceptionHandler<AssemblerException> {

    private static final DynamicCommandExceptionType TYPE = new DynamicCommandExceptionType(o -> (Message) o);

    @Override
    public Class<AssemblerException> exceptionType() {
        return AssemblerException.class;
    }

    @Override
    public CommandSyntaxException handle(AssemblerException exception) {
        Component<?> component = exception.getComponent();
        Message msg = exception.getCause().getDynamicMessage();
        if (component != null) {
            CommandContext<?> ctx = exception.getContext().commandContext();
            StringReader reader = new StringReader(ctx.getInput());
            reader.setCursor(component.range().getStart());
            return TYPE.createWithContext(reader, msg);
        } else {
            return TYPE.create(msg);
        }
    }

}
