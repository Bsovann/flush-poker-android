package com.example.flush_poker_android.Logic;

import java.io.Serializable;

/**
 * This class represents a playing card with a specific rank and suit.
 * It is used in the poker game to create decks, hands, and community cards.
 *
 * @author Bondith Sovann
 */
public class Card implements Serializable {
    private final Rank rank;
    private final Suit suit;

    /**
     * Constructor for creating a Card instance with the specified rank and suit.
     *
     * @param rank The rank of the card (e.g., Ace, King, 2).
     * @param suit The suit of the card (e.g., Hearts, Diamonds, Clubs).
     */
    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    /**
     * Constructor for creating a Card instance with the specified suit and rank.
     * This constructor is provided for convenience.
     *
     * @param suit The suit of the card (e.g., Hearts, Diamonds, Clubs).
     * @param rank The rank of the card (e.g., Ace, King, 2).
     */
    public Card(Suit suit, Rank rank) {
        this.rank = rank;
        this.suit = suit;
    }

    /**
     * Gets the rank of the card.
     *
     * @return The rank of the card.
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Gets the suit of the card.
     *
     * @return The suit of the card.
     */
    public Suit getSuit() {
        return suit;
    }

    /**
     * Returns a string representation of the card in the format "rank_of_suit".
     *
     * @return A string representing the card.
     */
    @Override
    public String toString() {
        return rank.getName().toLowerCase() + "_of_" + suit.getName().toLowerCase();
    }
}
