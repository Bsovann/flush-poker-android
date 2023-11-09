package com.example.flush_poker_android;

import com.example.flush_poker_android.Logic.Card;
import com.example.flush_poker_android.Logic.Rank;
import com.example.flush_poker_android.Logic.Suit;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CardTest {

    @Test
    public void testGetRank() {
        Card card = new Card(Suit.HEARTS, Rank.ACE);
        assertEquals(Rank.ACE, card.getRank());
    }

    @Test
    public void testGetSuit() {
        Card card = new Card(Suit.DIAMONDS, Rank.KING);
        assertEquals(Suit.DIAMONDS, card.getSuit());
    }

    @Test
    public void testToString() {
        Card card = new Card(Suit.CLUBS, Rank.QUEEN);
        // The toString method should return a string in the format "rank_name_of_suit_name"
        assertEquals("queen_of_clubs", card.toString());
    }
}

