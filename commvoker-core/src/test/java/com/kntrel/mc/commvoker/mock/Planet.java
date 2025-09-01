package com.kntrel.mc.commvoker.mock;

public final class Planet {

    //CONSTANTS
    public static final Planet
        EARTH = new Planet("Earth"),
        TATOOINE = new Planet("Tatooine"),
        HOTH = new Planet("Hoth"),
        MARS = new Planet("Mars"),
        MANDALORE = new Planet("Mandalore"),
        ARRAKIS = new Planet("Arrakis"),
        KRYPTON = new Planet("Krypton");

    public static Planet[] values() {
        return new Planet[] { EARTH, TATOOINE, HOTH, MARS, MANDALORE, ARRAKIS, KRYPTON };
    }


    //FIELDS
    private String name;

    //CONSTRUCTORS
    private Planet(String name) { this.name = name; }

    //GETTERS
    public String getName() { return name; }
}
