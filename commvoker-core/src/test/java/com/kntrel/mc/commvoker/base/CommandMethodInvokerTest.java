package com.kntrel.mc.commvoker.base;

import com.kntrel.mc.commvoker.command.Command;
import com.kntrel.mc.commvoker.mock.MockCommvoker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandMethodInvokerTest {

    private static final Object SRC = new Object();

    private MockCommvoker commvoker;


    @BeforeEach
    void setUp() {
        this.commvoker = new MockCommvoker();
        this.commvoker.register(new NumericReturnHolder());
    }


    @Test
    void primitiveByteReturnBecomesCommandResult() {
        assertEquals(11, assertDoesNotThrow(() -> this.commvoker.execute("primitive-byte-return", SRC)));
    }

    @Test
    void boxedByteReturnBecomesCommandResult() {
        assertEquals(12, assertDoesNotThrow(() -> this.commvoker.execute("boxed-byte-return", SRC)));
    }

    @Test
    void primitiveShortReturnBecomesCommandResult() {
        assertEquals(13, assertDoesNotThrow(() -> this.commvoker.execute("primitive-short-return", SRC)));
    }

    @Test
    void boxedShortReturnBecomesCommandResult() {
        assertEquals(14, assertDoesNotThrow(() -> this.commvoker.execute("boxed-short-return", SRC)));
    }

    @Test
    void primitiveIntReturnBecomesCommandResult() {
        assertEquals(15, assertDoesNotThrow(() -> this.commvoker.execute("primitive-int-return", SRC)));
    }

    @Test
    void boxedIntegerReturnBecomesCommandResult() {
        assertEquals(16, assertDoesNotThrow(() -> this.commvoker.execute("boxed-integer-return", SRC)));
    }

    @Test
    void primitiveLongReturnBecomesCommandResult() {
        assertEquals(17, assertDoesNotThrow(() -> this.commvoker.execute("primitive-long-return", SRC)));
    }

    @Test
    void boxedLongReturnBecomesCommandResult() {
        assertEquals(18, assertDoesNotThrow(() -> this.commvoker.execute("boxed-long-return", SRC)));
    }

    @Test
    void primitiveFloatReturnBecomesCommandResult() {
        assertEquals(19, assertDoesNotThrow(() -> this.commvoker.execute("primitive-float-return", SRC)));
    }

    @Test
    void boxedFloatReturnBecomesCommandResult() {
        assertEquals(20, assertDoesNotThrow(() -> this.commvoker.execute("boxed-float-return", SRC)));
    }

    @Test
    void negativePrimitiveFloatReturnBecomesCommandResult() {
        assertEquals(-19, assertDoesNotThrow(() -> this.commvoker.execute("negative-primitive-float-return", SRC)));
    }

    @Test
    void negativeBoxedFloatReturnBecomesCommandResult() {
        assertEquals(-20, assertDoesNotThrow(() -> this.commvoker.execute("negative-boxed-float-return", SRC)));
    }

    @Test
    void primitiveDoubleReturnBecomesCommandResult() {
        assertEquals(21, assertDoesNotThrow(() -> this.commvoker.execute("primitive-double-return", SRC)));
    }

    @Test
    void boxedDoubleReturnBecomesCommandResult() {
        assertEquals(22, assertDoesNotThrow(() -> this.commvoker.execute("boxed-double-return", SRC)));
    }

    @Test
    void negativePrimitiveDoubleReturnBecomesCommandResult() {
        assertEquals(-21, assertDoesNotThrow(() -> this.commvoker.execute("negative-primitive-double-return", SRC)));
    }

    @Test
    void negativeBoxedDoubleReturnBecomesCommandResult() {
        assertEquals(-22, assertDoesNotThrow(() -> this.commvoker.execute("negative-boxed-double-return", SRC)));
    }


    public static class NumericReturnHolder {

        @Command("primitive-byte-return")
        public byte primitiveByte() {
            return 11;
        }

        @Command("boxed-byte-return")
        public Byte boxedByte() {
            return 12;
        }

        @Command("primitive-short-return")
        public short primitiveShort() {
            return 13;
        }

        @Command("boxed-short-return")
        public Short boxedShort() {
            return 14;
        }

        @Command("primitive-int-return")
        public int primitiveInt() {
            return 15;
        }

        @Command("boxed-integer-return")
        public Integer boxedInteger() {
            return 16;
        }

        @Command("primitive-long-return")
        public long primitiveLong() {
            return 17L;
        }

        @Command("boxed-long-return")
        public Long boxedLong() {
            return 18L;
        }

        @Command("primitive-float-return")
        public float primitiveFloat() {
            return 19.9f;
        }

        @Command("boxed-float-return")
        public Float boxedFloat() {
            return 20.9f;
        }

        @Command("negative-primitive-float-return")
        public float negativePrimitiveFloat() {
            return -19.9f;
        }

        @Command("negative-boxed-float-return")
        public Float negativeBoxedFloat() {
            return -20.9f;
        }

        @Command("primitive-double-return")
        public double primitiveDouble() {
            return 21.9d;
        }

        @Command("boxed-double-return")
        public Double boxedDouble() {
            return 22.9d;
        }

        @Command("negative-primitive-double-return")
        public double negativePrimitiveDouble() {
            return -21.9d;
        }

        @Command("negative-boxed-double-return")
        public Double negativeBoxedDouble() {
            return -22.9d;
        }
    }
}
