package com.example.flush_poker_android;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.flush_poker_android.Logic.Rank;

public class RankTest {

    @Test
    public void testRankName() {
        assertEquals("Ace", Rank.ACE.getName());
        assertEquals("King", Rank.KING.getName());
        assertEquals("Queen", Rank.QUEEN.getName());
        assertEquals("Jack", Rank.JACK.getName());
        assertEquals("Ten", Rank.TEN.getName());
        assertEquals("Nine", Rank.NINE.getName());
        assertEquals("Eight", Rank.EIGHT.getName());
        assertEquals("Seven", Rank.SEVEN.getName());
        assertEquals("Six", Rank.SIX.getName());
        assertEquals("Five", Rank.FIVE.getName());
        assertEquals("Four", Rank.FOUR.getName());
        assertEquals("Three", Rank.THREE.getName());
        assertEquals("Two", Rank.TWO.getName());
    }

    @Test
    public void testRankValue() {
        assertEquals(14, Rank.ACE.getValue());
        assertEquals(13, Rank.KING.getValue());
        assertEquals(12, Rank.QUEEN.getValue());
        assertEquals(11, Rank.JACK.getValue());
        assertEquals(10, Rank.TEN.getValue());
        assertEquals(9, Rank.NINE.getValue());
        assertEquals(8, Rank.EIGHT.getValue());
        assertEquals(7, Rank.SEVEN.getValue());
        assertEquals(6, Rank.SIX.getValue());
        assertEquals(5, Rank.FIVE.getValue());
        assertEquals(4, Rank.FOUR.getValue());
        assertEquals(3, Rank.THREE.getValue());
        assertEquals(2, Rank.TWO.getValue());
    }

    @Test
    public void testRankEquality() {
        assertEquals(Rank.ACE, Rank.ACE);
        assertEquals(Rank.KING, Rank.KING);
        assertEquals(Rank.QUEEN, Rank.QUEEN);
        assertEquals(Rank.JACK, Rank.JACK);
        assertEquals(Rank.TEN, Rank.TEN);
        assertEquals(Rank.NINE, Rank.NINE);
        assertEquals(Rank.EIGHT, Rank.EIGHT);
        assertEquals(Rank.SEVEN, Rank.SEVEN);
        assertEquals(Rank.SIX, Rank.SIX);
        assertEquals(Rank.FIVE, Rank.FIVE);
        assertEquals(Rank.FOUR, Rank.FOUR);
        assertEquals(Rank.THREE, Rank.THREE);
        assertEquals(Rank.TWO, Rank.TWO);
    }

    @Test
    public void testRankInequality() {
        assertNotEquals(Rank.ACE, Rank.KING);
        assertNotEquals(Rank.KING, Rank.QUEEN);
        assertNotEquals(Rank.QUEEN, Rank.JACK);
        assertNotEquals(Rank.JACK, Rank.TEN);
        assertNotEquals(Rank.TEN, Rank.NINE);
        assertNotEquals(Rank.NINE, Rank.EIGHT);
        assertNotEquals(Rank.EIGHT, Rank.SEVEN);
        assertNotEquals(Rank.SEVEN, Rank.SIX);
        assertNotEquals(Rank.SIX, Rank.FIVE);
        assertNotEquals(Rank.FIVE, Rank.FOUR);
        assertNotEquals(Rank.FOUR, Rank.THREE);
        assertNotEquals(Rank.THREE, Rank.TWO);
    }
}

