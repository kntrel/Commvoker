package com.kntrel.mc.commvoker.assembler;

import com.kntrel.mc.commvoker.command.Command;
import com.kntrel.mc.commvoker.argument.binder.ArgumentBinder;
import com.kntrel.mc.commvoker.mock.GroupAssembler;
import com.kntrel.mc.commvoker.mock.Group;
import com.kntrel.mc.commvoker.mock.MockCommvoker;
import com.kntrel.mc.commvoker.mock.Person;
import com.kntrel.mc.commvoker.mock.PersonAssembler;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static com.kntrel.mc.commvoker.test.Assertions.*;

public class ComposedAssemblerTest {

    public static class Holder {

        Person person;
        List<Person> people;

        Group group;

        Person[] peopleArray;

        @Command("person set {person}")
        public void setPerson(Person person) {
            this.person = person;
        }

        @Command("person set many {persons}")
        public void setPeople(List<Person> people) {
            this.people = people;
        }

        @Command("group set {group}")
        public void setGroup(Group group) {
            this.group = group;
        }

        @Command("person set array {persons}")
        public void setPeople(Person[] people) {
            this.peopleArray = people;
        }
    }


    MockCommvoker commvoker;
    Holder holder;
    Object src;

    private ComposedAssemblerTest() {
        this.commvoker = new MockCommvoker();
        this.holder = new Holder();
        this.src = new Object();

        commvoker.getArgumentRegistry().register(
                ArgumentBinder.argumentAssembler(() -> new PersonAssembler())
                        .toClass(Person.class)
                        .bind()
        );
        commvoker.getArgumentRegistry().register(
                ArgumentBinder.argumentAssembler(GroupAssembler::new)
                        .toClass(Group.class)
                        .bind()
        );
        this.commvoker.register(this.holder);
    }


    @Test
    void doubleComposedArgument() {
        assertHasUsage(this.commvoker.getCommandDispatcher(), "person set <person> <person1>");

        assertDoesNotThrow(() -> this.commvoker.execute("person set Chespirito 98", this.src));
        assertNotNull(this.holder.person);
        assertEquals(new Person("Chespirito", 98), this.holder.person);
    }

    @Test
    void composedArgumentCollection() {

        Person[] people = new Person[] { new Person("Chespirito", 98), new Person("Don Ramón", 110), new Person("Gandalf", 100) };

        assertDoesNotThrow(() -> this.commvoker.execute("person set many Chespirito 98 \"Don Ramón\" 110 and Gandalf 100", this.src));
        assertNotNull(this.holder.people);
        assertEquals(3, this.holder.people.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(people[i], this.holder.people.get(i));
        }
    }

    @Test
    void multiComposedGArgument() {
        assertDoesNotThrow(() -> this.commvoker.execute("group set Lokillos Chespirito 98 \"Don Ramón\" 110 and Gandalf 100", this.src));
        assertNotNull(this.holder.group);
        assertEquals(3, this.holder.group.people().size());
        assertEquals("Lokillos", this.holder.group.name());

        Person[] people = new Person[] { new Person("Chespirito", 98), new Person("Don Ramón", 110), new Person("Gandalf", 100) };
        int i = 0;
        for (Person p : this.holder.group.people()) {
            assertEquals(people[i++], p);
        }
    }

    @Test
    void arrayArgument() {
        Person[] people = new Person[] { new Person("Chespirito", 98), new Person("Don Ramón", 110), new Person("Gandalf", 100) };

        assertDoesNotThrow(() -> this.commvoker.execute("person set array Chespirito 98 \"Don Ramón\" 110 and Gandalf 100", this.src));
        assertNotNull(this.holder.peopleArray);
        assertEquals(3, this.holder.peopleArray.length);
        for (int i = 0; i < 3; i++) {
            assertEquals(people[i], this.holder.peopleArray[i]);
        }
    }
}