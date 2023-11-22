package com.example.flush_poker_android.Logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Deck implements Serializable {
    private Stack<Card> cards;

    public Deck() {
        cards = new Stack<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.push(new Card(rank, suit));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card dealCard() {
        if (cards.isEmpty()) {
            // Handle out of cards situation
            return null;
        }
        return cards.remove(0);
    }

    public Collection<? extends Card> draw(int i) {
        List<Card> drawCards = new ArrayList<>();
        for(int j = 0; j < i; j++)
            drawCards.add(cards.pop());

        return drawCards;
    }

    public void reset() {
        cards = new Stack<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
    }

    public int size() {
        return cards.size();
    }
}

