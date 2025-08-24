package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.command.Command;
import com.kntrel.mc.commvoker.mock.MockCommvoker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnumAssemblerTest {

    public enum MockEnum { CONSTANT_1, CONSTANT_2, CONSTANT_3 }

    private static final Object SRC = new Object();
    private MockEnum instance;
    private MockCommvoker commvoker;

    @Command("enum {const}")
    public void setEnum(MockEnum mockEnum) {
        this.instance = mockEnum;
    }

    @BeforeEach public void setup() {
        this.commvoker = new MockCommvoker();
        this.commvoker.register(this);
    }

    @Test public void enumArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("enum CONSTANT_1", SRC));
        assertEquals(MockEnum.CONSTANT_1, this.instance);

        assertDoesNotThrow(() -> this.commvoker.execute("enum CONSTANT_2", SRC));
        assertEquals(MockEnum.CONSTANT_2, this.instance);

        assertDoesNotThrow(() -> this.commvoker.execute("enum CONSTANT_3", SRC));
        assertEquals(MockEnum.CONSTANT_3, this.instance);
    }
}
