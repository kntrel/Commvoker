package com.kntrel.mc.commvoker.callback;

import com.kntrel.mc.commvoker.command.Command;
import com.kntrel.mc.commvoker.command.CommandMethodContext;
import com.kntrel.mc.commvoker.mock.MockCommvoker;
import com.kntrel.mc.commvoker.mock.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReturnCallbackTest {

    //HOLDER
    private static final Object SRC = new Object();
    public class Holder {

        @Command
        public String greet() {
            return "Hello, World!";
        }

        @Command
        public String echo(String message) {
            return message;
        }

        @Command
        public int add(int a, int b) {
            return a + b;
        }

        @Command("no-return")
        public void noReturn() {
            // does nothing
        }

        @Command
        public Person person(String name, int age) {
            return new Person(name, age);
        }

        @Command("list-numbers")
        public List<?> listOfNumbers() {
            return List.of(1, 2, 3, 4, 5);
        }

        @Command("list-persons")
        public List<Person> listOfPersons() {
            return List.of(new Person("Alice", 25), new Person("Bob", 35));
        }
    }

    //FIELDS
    private MockCommvoker commvoker;
    private String text = null;
    private int number = 0;
    private Person person = null;
    private List<?> list = null;
    private List<Person> personList = null;


    //CALLBACKS
    private final class StringReturnCallback implements ReturnCallback<Object, String> {

        @Override
        public void onReturn(CommandMethodContext<?> context, String returnValue) {
            ReturnCallbackTest.this.text = returnValue;
        }
    }
    private final class IntegerReturnCallback implements ReturnCallback<Object, Integer> {

        @Override
        public void onReturn(CommandMethodContext<?> context, Integer returnValue) {
            ReturnCallbackTest.this.number = returnValue;
        }
    }
    private final class PersonCallBack implements ReturnCallback<Object, Person> {

        @Override
        public void onReturn(CommandMethodContext<?> context, Person returnValue) {
            ReturnCallbackTest.this.person = returnValue;
        }
    }
    private final class ListReturnCallback implements ReturnCallback<Object, List<?>> {

        @Override
        public void onReturn(CommandMethodContext<?> context, List<?> returnValue) {
            ReturnCallbackTest.this.list = returnValue;
        }
    }
    private final class PersonListReturnCallback implements ReturnCallback<Object, List<Person>> {

        @Override
        public void onReturn(CommandMethodContext<?> context, List<Person> returnValue) {
            ReturnCallbackTest.this.personList = returnValue;
        }
    }


    @BeforeEach
    void setup() {
        this.commvoker = new MockCommvoker();
        this.commvoker.register(new Holder());
    }


    @Test void stringReturnCallbackWorks() {
        this.text = null;

        assertDoesNotThrow(() -> this.commvoker.execute("greet", SRC));
        assertNull(this.text);

        assertDoesNotThrow(() -> this.commvoker.execute("echo hi", SRC));
        assertNull(this.text);

        this.commvoker.registerCallback(new StringReturnCallback());

        assertDoesNotThrow(() -> this.commvoker.execute("greet", SRC));
        assertEquals("Hello, World!", this.text);

        assertDoesNotThrow(() -> this.commvoker.execute("echo hi", SRC));
        assertEquals("hi", this.text);
    }

    @Test void integerReturnCallbackWorks() {
        this.number = 0;

        assertDoesNotThrow(() -> this.commvoker.execute("add 2 3", SRC));
        assertEquals(0, this.number);

        this.commvoker.registerCallback(new IntegerReturnCallback());

        assertDoesNotThrow(() -> this.commvoker.execute("add 2 3", SRC));
        assertEquals(5, this.number);
    }

    @Test void personReturnCallbackWorks() {
        this.person = null;

        assertDoesNotThrow(() -> this.commvoker.execute("person John 30", SRC));
        assertNull(this.person);

        this.commvoker.registerCallback(new PersonCallBack());

        assertDoesNotThrow(() -> this.commvoker.execute("person John 30", SRC));
        assertNotNull(this.person);
        assertEquals("John", this.person.name());
        assertEquals(30, this.person.age());
    }

    @Test void listReturnCallbackWorks() {
        this.list = null;

        assertDoesNotThrow(() -> this.commvoker.execute("list-numbers", SRC));
        assertNull(this.list);

        this.commvoker.registerCallback(new ListReturnCallback());

        assertDoesNotThrow(() -> this.commvoker.execute("list-numbers", SRC));
        assertNotNull(this.list);
        assertEquals(List.of(1, 2, 3, 4, 5), this.list);

        assertDoesNotThrow(() -> this.commvoker.execute("list-persons", SRC));
        assertEquals(2, this.list.size());
        assertEquals(new Person("Alice", 25), this.list.get(0));
        assertEquals(new Person("Bob", 35), this.list.get(1));
    }

    @Test void personListReturnCallbackWorks() {
        this.list = null;
        this.personList = null;

        assertDoesNotThrow(() -> this.commvoker.execute("list-persons", SRC));
        assertNull(this.personList);
        assertNull(this.list);

        this.commvoker.registerCallback(new PersonListReturnCallback());

        assertDoesNotThrow(() -> this.commvoker.execute("list-persons", SRC));
        assertNotNull(this.personList);
        assertEquals(2, this.personList.size());
        assertEquals("Alice", this.personList.get(0).name());
        assertEquals(25, this.personList.get(0).age());
        assertEquals("Bob", this.personList.get(1).name());
        assertEquals(35, this.personList.get(1).age());
        assertNull(this.list);
    }

    @Test void voidReturnDoesNotTriggerCallback() {
        this.text = null;

        assertDoesNotThrow(() -> this.commvoker.execute("no-return", SRC));
        assertNull(this.text);

        this.commvoker.registerCallback(new StringReturnCallback());

        assertDoesNotThrow(() -> this.commvoker.execute("no-return", SRC));
        assertNull(this.text);
    }

    @Test void typeCallbackLinkingWorks() {
        this.text = null;
        this.number = 0;
        this.list = null;

        this.commvoker = new MockCommvoker();
        this.commvoker.registerCallback(new StringReturnCallback());
        this.commvoker.registerCallback(new IntegerReturnCallback());
        this.commvoker.registerCallback(new ListReturnCallback());
        this.commvoker.register(new Holder());

        assertDoesNotThrow(() -> this.commvoker.execute("greet", SRC));
        assertEquals("Hello, World!", this.text);
        assertEquals(0, this.number);
        assertNull(this.list);

        assertDoesNotThrow(() -> this.commvoker.execute("add 10 15", SRC));
        assertEquals("Hello, World!", this.text);
        assertEquals(25, this.number);
        assertNull(this.list);

        assertDoesNotThrow(() -> this.commvoker.execute("list-numbers", SRC));
        assertEquals("Hello, World!", this.text);
        assertEquals(25, this.number);
        assertEquals(List.of(1, 2, 3, 4, 5), this.list);

    }
}
