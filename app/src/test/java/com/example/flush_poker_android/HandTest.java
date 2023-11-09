package com.example.flush_poker_android;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.flush_poker_android.Logic.Card;
import com.example.flush_poker_android.Logic.Hand;
import com.example.flush_poker_android.Logic.Rank;
import com.example.flush_poker_android.Logic.Suit;

import java.util.ArrayList;
import java.util.List;

public class HandTest {

    private Hand hand;

    @Before
    public void setUp() {
        hand = new Hand();
    }

    @Test
    public void testAddCard() {
        Card card = new Card(Suit.HEARTS, Rank.ACE);
        hand.addCard(card);
        assertTrue(hand.getCards().contains(card));
    }

    @Test
    public void testClear() {
        Card card = new Card(Suit.HEARTS, Rank.ACE);
        hand.addCard(card);
        hand.clear();
        assertTrue(hand.getCards().isEmpty());
    }

    @Test
    public void testCompareHandsRoyalFlush() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.HEARTS, Rank.KING));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(new Card(Suit.HEARTS, Rank.QUEEN));
        communityCards.add(new Card(Suit.HEARTS, Rank.JACK));
        communityCards.add(new Card(Suit.HEARTS, Rank.TEN));
        communityCards.add(new Card(Suit.HEARTS, Rank.NINE));
        communityCards.add(new Card(Suit.HEARTS, Rank.EIGHT));

        assertEquals(1, hand.compareHands(communityCards));
    }

    @Test
    public void testCompareHandsStraightFlush() {
        hand.addCard(new Card(Suit.DIAMONDS, Rank.NINE));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.EIGHT));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(new Card(Suit.DIAMONDS, Rank.SEVEN));
        communityCards.add(new Card(Suit.DIAMONDS, Rank.SIX));
        communityCards.add(new Card(Suit.DIAMONDS, Rank.FIVE));
        communityCards.add(new Card(Suit.DIAMONDS, Rank.FOUR));
        communityCards.add(new Card(Suit.CLUBS, Rank.THREE));

        assertEquals(2, hand.compareHands(communityCards));
    }

    @Test
    public void testCompareHandsFourOfAKind() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.ACE));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(new Card(Suit.CLUBS, Rank.ACE));
        communityCards.add(new Card(Suit.SPADES, Rank.ACE));
        communityCards.add(new Card(Suit.HEARTS, Rank.KING));
        communityCards.add(new Card(Suit.DIAMONDS, Rank.KING));
        communityCards.add(new Card(Suit.HEARTS, Rank.QUEEN));

        assertEquals(3, hand.compareHands(communityCards));
    }

    @Test
    public void testCompareHandsFullHouse() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.DIAMONDS, Rank.ACE));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(new Card(Suit.CLUBS, Rank.ACE));
        communityCards.add(new Card(Suit.SPADES, Rank.KING));
        communityCards.add(new Card(Suit.HEARTS, Rank.KING));
        communityCards.add(new Card(Suit.DIAMONDS, Rank.JACK));
        communityCards.add(new Card(Suit.HEARTS, Rank.QUEEN));

        assertEquals(4, hand.compareHands(communityCards));
    }

    @Test
    public void testCompareHandsFlush() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.HEARTS, Rank.KING));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(new Card(Suit.HEARTS, Rank.FOUR));
        communityCards.add(new Card(Suit.HEARTS, Rank.SEVEN));
        communityCards.add(new Card(Suit.CLUBS, Rank.EIGHT));
        communityCards.add(new Card(Suit.HEARTS, Rank.JACK));
        communityCards.add(new Card(Suit.SPADES, Rank.QUEEN));

        assertEquals(5, hand.compareHands(communityCards));
    }

    @Test
    public void testCompareHandsStraight() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.SPADES, Rank.KING));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(new Card(Suit.CLUBS, Rank.QUEEN));
        communityCards.add(new Card(Suit.DIAMONDS, Rank.JACK));
        communityCards.add(new Card(Suit.HEARTS, Rank.TEN));
        communityCards.add(new Card(Suit.HEARTS, Rank.NINE));
        communityCards.add(new Card(Suit.SPADES, Rank.EIGHT));

        assertEquals(6, hand.compareHands(communityCards));
    }

    @Test
    public void testCompareHandsThreeOfAKind() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.SPADES, Rank.ACE));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(new Card(Suit.CLUBS, Rank.ACE));
        communityCards.add(new Card(Suit.DIAMONDS, Rank.KING));
        communityCards.add(new Card(Suit.HEARTS, Rank.QUEEN));
        communityCards.add(new Card(Suit.SPADES, Rank.QUEEN));
        communityCards.add(new Card(Suit.CLUBS, Rank.QUEEN));

        assertEquals(7, hand.compareHands(communityCards));
    }

    @Test
    public void testCompareHandsTwoPairs() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.SPADES, Rank.ACE));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(new Card(Suit.CLUBS, Rank.KING));
        communityCards.add(new Card(Suit.DIAMONDS, Rank.KING));
        communityCards.add(new Card(Suit.HEARTS, Rank.QUEEN));
        communityCards.add(new Card(Suit.SPADES, Rank.JACK));
        communityCards.add(new Card(Suit.CLUBS, Rank.SEVEN));

        assertEquals(8, hand.compareHands(communityCards));
    }

    @Test
    public void testCompareHandsPair() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.SPADES, Rank.ACE));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(new Card(Suit.CLUBS, Rank.NINE));
        communityCards.add(new Card(Suit.DIAMONDS, Rank.KING));
        communityCards.add(new Card(Suit.HEARTS, Rank.JACK));
        communityCards.add(new Card(Suit.SPADES, Rank.TEN));
        communityCards.add(new Card(Suit.CLUBS, Rank.EIGHT));

        assertEquals(9, hand.compareHands(communityCards));
    }

    @Test
    public void testCompareHandsHighCard() {
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.SPADES, Rank.KING));

        List<Card> communityCards = new ArrayList<>();
        communityCards.add(new Card(Suit.CLUBS, Rank.FOUR));
        communityCards.add(new Card(Suit.DIAMONDS, Rank.JACK));
        communityCards.add(new Card(Suit.HEARTS, Rank.TEN));
        communityCards.add(new Card(Suit.SPADES, Rank.NINE));
        communityCards.add(new Card(Suit.CLUBS, Rank.EIGHT));

        assertEquals(10, hand.compareHands(communityCards));
    }

    @Test
    public void testClearHand() {
        Card card = new Card(Suit.HEARTS, Rank.ACE);
        hand.addCard(card);
        hand.clearHand();
        assertTrue(hand.getCards().isEmpty());
    }
}


