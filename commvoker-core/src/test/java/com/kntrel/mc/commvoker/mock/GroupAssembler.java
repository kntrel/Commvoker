package com.kntrel.mc.commvoker.mock;

import com.kntrel.mc.commvoker.assembler.Assembler;
import com.kntrel.mc.commvoker.assembler.BiComposedAssembler;
import com.kntrel.mc.commvoker.provided.assemblers.CollectionAssembler;
import com.kntrel.mc.commvoker.provided.assemblers.StringAssembler;
import com.mojang.brigadier.context.CommandContext;

import java.util.List;

public class GroupAssembler implements BiComposedAssembler<Object, String, List<Person>, Group> {
    @Override
    public Assembler<? super Object, ? extends String> firstDelegate() {
        return StringAssembler.string();
    }

    @Override
    public Assembler<? super Object, ? extends List<Person>> secondDelegate() {
        return CollectionAssembler.listOf(new PersonAssembler());
    }

    @Override
    public Group compose(CommandContext<?> ctx, String first, List<Person> second) {
        return new Group(first, second);
    }
}
