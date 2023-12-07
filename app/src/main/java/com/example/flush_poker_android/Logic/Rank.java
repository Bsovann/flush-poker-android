package com.example.flush_poker_android.Logic;

import java.io.Serializable;

/**
 * The Rank enum represents the rank of a playing card in a standard deck.
 * Each rank has a name and a corresponding numeric value.
 *
 * @author Bondith Sovann
 */
public enum Rank implements Serializable {
    ACE("Ace", 14),
    KING("King", 13),
    QUEEN("Queen", 12),
    JACK("Jack", 11),
    TEN("Ten", 10),
    NINE("Nine", 9),
    EIGHT("Eight", 8),
    SEVEN("Seven", 7),
    SIX("Six", 6),
    FIVE("Five", 5),
    FOUR("Four", 4),
    THREE("Three", 3),
    TWO("Two", 2);

    private final String name;
    private final int value;

    /**
     * Constructs a rank with a given name and value.
     *
     * @param name  The name of the rank.
     * @param value The numeric value of the rank.
     */
    Rank(String name, int value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Get the name of the rank.
     *
     * @return The name of the rank.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the numeric value of the rank.
     *
     * @return The numeric value of the rank.
     */
    public int getValue() {
        return value;
    }
}
