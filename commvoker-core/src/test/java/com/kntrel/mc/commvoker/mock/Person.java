package com.kntrel.mc.commvoker.mock;

public class Person {
    //FIELDS
    private String name;
    private int age;


    //CONSTRUCTOR
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }


    //SETTERS
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }


    //GETTERS
    public String name() { return name; }
    public int age() { return age; }


    //IMPLEMENTATION
    @Override public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null) { return false; }
        if (!(o instanceof Person p)) { return false; }
        return this.name.equals(p.name) && this.age == p.age;
    }
}
