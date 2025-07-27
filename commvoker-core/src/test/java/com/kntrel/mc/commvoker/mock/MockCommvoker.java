package com.kntrel.mc.commvoker.mock;

import com.kntrel.mc.commvoker.base.BaseCommvoker;
import com.mojang.brigadier.CommandDispatcher;

public class MockCommvoker extends BaseCommvoker<Object> {

    public MockCommvoker() {
        super(new CommandDispatcher<>());
    }


    public CommandDispatcher<Object> getCommandDispatcher() {
        return super.getCommandDispatcher();
    }
}
