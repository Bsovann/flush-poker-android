package com.example.flush_poker_android;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.flush_poker_android.Logic.Suit;

public class SuitTest {

    @Test
    public void testSuitName() {
        assertEquals("Hearts", Suit.HEARTS.getName());
        assertEquals("Diamonds", Suit.DIAMONDS.getName());
        assertEquals("Clubs", Suit.CLUBS.getName());
        assertEquals("Spades", Suit.SPADES.getName());
    }

    @Test
    public void testSuitEquality() {
        assertEquals(Suit.HEARTS, Suit.HEARTS);
        assertEquals(Suit.DIAMONDS, Suit.DIAMONDS);
        assertEquals(Suit.CLUBS, Suit.CLUBS);
        assertEquals(Suit.SPADES, Suit.SPADES);
    }

    @Test
    public void testSuitInequality() {
        assertNotEquals(Suit.HEARTS, Suit.DIAMONDS);
        assertNotEquals(Suit.DIAMONDS, Suit.CLUBS);
        assertNotEquals(Suit.CLUBS, Suit.SPADES);
        assertNotEquals(Suit.SPADES, Suit.HEARTS);
    }
}

