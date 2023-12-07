package com.example.flush_poker_android.Logic;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * The Deck class represents a standard deck of playing cards.
 * It can be used to shuffle, deal cards, draw multiple cards, reset the deck, and check its size.
 *
 * @author Bondith Sovann
 */
public class Deck implements Serializable {
    private Stack<Card> cards;

    /**
     * Constructs a new deck of cards and initializes it with all possible cards.
     */
    public Deck() {
        cards = new Stack<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.push(new Card(rank, suit));
            }
        }
    }

    /**
     * Shuffles the cards in the deck randomly.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Deals a single card from the deck. Returns null if the deck is empty.
     *
     * @return The dealt card, or null if there are no cards left.
     */
    public Card dealCard() {
        if (cards.isEmpty()) {
            // Handle the out of cards situation
            return null;
        }
        return cards.remove(0);
    }

    /**
     * Draws a specified number of cards from the deck.
     *
     * @param count The number of cards to draw.
     * @return A list of drawn cards.
     */
    public List<Card> draw(int count) {
        List<Card> drawCards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            drawCards.add(cards.pop());
        }
        return drawCards;
    }

    /**
     * Resets the deck by recreating it with all possible cards.
     */
    public void reset() {
        cards = new Stack<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
    }

    /**
     * Returns the current size of the deck.
     *
     * @return The number of cards in the deck.
     */
    public int size() {
        return cards.size();
    }
}
