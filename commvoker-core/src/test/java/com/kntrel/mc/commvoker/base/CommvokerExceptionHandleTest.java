package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.command.Command;
import com.kntrel.mc.commvoker.mock.MockCommvoker;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommvokerExceptionHandleTest {

    //ASSETS
    private static final DynamicCommandExceptionType COMMAND_EXCEPTION_TYPE = new DynamicCommandExceptionType(o -> new LiteralMessage(o.toString()));

    public static class Holder {
        @Command("test")
        public void test() {
            throw new RuntimeException("error");
        }
    }


    //FIELDS
    private MockCommvoker commvoker;


    //SETUP
    @BeforeEach void setUp() {
        this.commvoker = new MockCommvoker();
        this.commvoker.register(new Holder());
    }


    //TEST
    @Test void testNoHandler() {
        assertThrows(RuntimeException.class, () -> this.commvoker.execute("test", new Object()));
    }

    @Test void testWithHandler() {
        this.commvoker.registerExceptionHandler(RuntimeException.class, e -> COMMAND_EXCEPTION_TYPE.create(e.getMessage()));

        CommandSyntaxException ex = assertThrows(CommandSyntaxException.class, () -> this.commvoker.execute("test", new Object()));
        assertEquals(ex.getMessage(), "error");
    }
}
