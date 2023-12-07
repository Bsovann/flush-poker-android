package com.example.flush_poker_android.Logic;

import java.io.Serializable;

/**
 * The Suit enum represents the suits of playing cards in a standard deck.
 * Each suit has a name.
 *
 * @author Bondith Sovann
 */
public enum Suit implements Serializable {
    HEARTS("Hearts"),
    DIAMONDS("Diamonds"),
    CLUBS("Clubs"),
    SPADES("Spades");

    private final String name;

    /**
     * Constructs a suit with a given name.
     *
     * @param name The name of the suit.
     */
    Suit(String name) {
        this.name = name;
    }

    /**
     * Get the name of the suit.
     *
     * @return The name of the suit.
     */
    public String getName() {
        return name;
    }
}
