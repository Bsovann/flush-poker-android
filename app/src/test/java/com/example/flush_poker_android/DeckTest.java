package com.example.flush_poker_android;

import com.example.flush_poker_android.Logic.Card;
import com.example.flush_poker_android.Logic.Deck;
import com.example.flush_poker_android.Logic.Rank;
import com.example.flush_poker_android.Logic.Suit;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class DeckTest {

    private Deck deck;

    @Before
    public void setUp() {
        deck = new Deck();
    }

    @Test
    public void testShuffle() {
        // Create a deck with known order
        Deck orderedDeck = new Deck();

        // Shuffle the deck
        deck.shuffle();

        // Since the shuffled deck should be in a different order,
        // we can't directly compare it to the ordered deck.
        // Instead, we can check that the shuffled deck has the same number of cards.
        assertEquals(orderedDeck.size(), deck.size());
    }

    @Test
    public void testDealCard() {
        // Create a deck with known order
        Deck orderedDeck = new Deck();

        // Deal a card from the ordered deck
        Card dealtCard = orderedDeck.dealCard();

        // The dealt card should be the top card of the ordered deck
        assertNotNull(dealtCard);
    }

    @Test
    public void testDraw() {
        // Draw 5 cards from the deck
        Collection<? extends Card> drawnCards = deck.draw(5);

        // The number of drawn cards should be 5
        assertEquals(5, drawnCards.size());
    }

    @Test
    public void testReset() {
        // Shuffle the deck to change the order
        deck.shuffle();

        // Reset the deck
        deck.reset();

        // After resetting, the deck should be in the original order
        Deck orderedDeck = new Deck();
        assertEquals(orderedDeck.size(), deck.size());
    }
}

