package com.kntrel.mc.commvoker.provided.assemblers;

import com.kntrel.mc.commvoker.command.Command;
import com.kntrel.mc.commvoker.mock.MockCommvoker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrimitiveAssemblerTest {

    private static final Object SRC = new Object();

    private PrimitiveHolder primitiveHolder;
    private BoxedHolder boxedHolder;
    private MockCommvoker commvoker;


    @BeforeEach
    void setUp() {
        this.primitiveHolder = new PrimitiveHolder();
        this.boxedHolder = new BoxedHolder();
        this.commvoker = new MockCommvoker();
        this.commvoker.register(this.primitiveHolder);
        this.commvoker.register(this.boxedHolder);
    }


    @Test
    void primitiveBooleanArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("primitive-boolean true", SRC));
        assertEquals(true, this.primitiveHolder.boolValue);
    }

    @Test
    void boxedBooleanArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("boxed-boolean false", SRC));
        assertEquals(Boolean.FALSE, this.boxedHolder.boolValue);
    }

    @Test
    void primitiveByteArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("primitive-byte 12", SRC));
        assertEquals((byte) 12, this.primitiveHolder.byteValue);
    }

    @Test
    void boxedByteArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("boxed-byte 13", SRC));
        assertEquals(Byte.valueOf((byte) 13), this.boxedHolder.byteValue);
    }

    @Test
    void primitiveShortArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("primitive-short 1234", SRC));
        assertEquals((short) 1234, this.primitiveHolder.shortValue);
    }

    @Test
    void boxedShortArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("boxed-short 1235", SRC));
        assertEquals(Short.valueOf((short) 1235), this.boxedHolder.shortValue);
    }

    @Test
    void primitiveIntArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("primitive-int-arg 44", SRC));
        assertEquals(44, this.primitiveHolder.intValue);
    }

    @Test
    void boxedIntArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("boxed-int 45", SRC));
        assertEquals(Integer.valueOf(45), this.boxedHolder.intValue);
    }

    @Test
    void primitiveLongArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("primitive-long 46", SRC));
        assertEquals(46L, this.primitiveHolder.longValue);
    }

    @Test
    void boxedLongArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("boxed-long-arg 47", SRC));
        assertEquals(Long.valueOf(47L), this.boxedHolder.longValue);
    }

    @Test
    void primitiveFloatArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("primitive-float 1.5", SRC));
        assertEquals(1.5f, this.primitiveHolder.floatValue, 0.0001f);
    }

    @Test
    void boxedFloatArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("boxed-float 2.5", SRC));
        assertEquals(Float.valueOf(2.5f), this.boxedHolder.floatValue);
    }

    @Test
    void negativePrimitiveFloatArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("primitive-float -1.5", SRC));
        assertEquals(-1.5f, this.primitiveHolder.floatValue, 0.0001f);
    }

    @Test
    void negativeBoxedFloatArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("boxed-float -2.5", SRC));
        assertEquals(Float.valueOf(-2.5f), this.boxedHolder.floatValue);
    }

    @Test
    void primitiveDoubleArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("primitive-double 3.5", SRC));
        assertEquals(3.5d, this.primitiveHolder.doubleValue, 0.0001d);
    }

    @Test
    void boxedDoubleArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("boxed-double 4.5", SRC));
        assertEquals(Double.valueOf(4.5d), this.boxedHolder.doubleValue);
    }

    @Test
    void negativePrimitiveDoubleArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("primitive-double -3.5", SRC));
        assertEquals(-3.5d, this.primitiveHolder.doubleValue, 0.0001d);
    }

    @Test
    void negativeBoxedDoubleArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("boxed-double -4.5", SRC));
        assertEquals(Double.valueOf(-4.5d), this.boxedHolder.doubleValue);
    }

    @Test
    void primitiveCharacterArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("primitive-char z", SRC));
        assertEquals('z', this.primitiveHolder.charValue);
    }

    @Test
    void boxedCharacterArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("boxed-char y", SRC));
        assertEquals(Character.valueOf('y'), this.boxedHolder.charValue);
    }


    public static class PrimitiveHolder {
        private boolean boolValue;
        private byte byteValue;
        private short shortValue;
        private int intValue;
        private long longValue;
        private float floatValue;
        private double doubleValue;
        private char charValue;

        @Command("primitive-boolean {value}")
        public void primitiveBoolean(boolean value) { this.boolValue = value; }

        @Command("primitive-byte {value}")
        public void primitiveByte(byte value) { this.byteValue = value; }

        @Command("primitive-short {value}")
        public void primitiveShort(short value) { this.shortValue = value; }

        @Command("primitive-int-arg {value}")
        public void primitiveInt(int value) { this.intValue = value; }

        @Command("primitive-long {value}")
        public void primitiveLong(long value) { this.longValue = value; }

        @Command("primitive-float {value}")
        public void primitiveFloat(float value) { this.floatValue = value; }

        @Command("primitive-double {value}")
        public void primitiveDouble(double value) { this.doubleValue = value; }

        @Command("primitive-char {value}")
        public void primitiveChar(char value) { this.charValue = value; }
    }

    public static class BoxedHolder {
        private Boolean boolValue;
        private Byte byteValue;
        private Short shortValue;
        private Integer intValue;
        private Long longValue;
        private Float floatValue;
        private Double doubleValue;
        private Character charValue;

        @Command("boxed-boolean {value}")
        public void boxedBoolean(Boolean value) { this.boolValue = value; }

        @Command("boxed-byte {value}")
        public void boxedByte(Byte value) { this.byteValue = value; }

        @Command("boxed-short {value}")
        public void boxedShort(Short value) { this.shortValue = value; }

        @Command("boxed-int {value}")
        public void boxedInt(Integer value) { this.intValue = value; }

        @Command("boxed-long-arg {value}")
        public void boxedLong(Long value) { this.longValue = value; }

        @Command("boxed-float {value}")
        public void boxedFloat(Float value) { this.floatValue = value; }

        @Command("boxed-double {value}")
        public void boxedDouble(Double value) { this.doubleValue = value; }

        @Command("boxed-char {value}")
        public void boxedChar(Character value) { this.charValue = value; }
    }
}
