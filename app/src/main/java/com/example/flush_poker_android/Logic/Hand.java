package com.example.flush_poker_android.Logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Hand {
    private List<Card> cards;

    public Hand() {
        cards = new ArrayList<>();
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void clear() {
        cards.clear();
    }

    public int compareHands(List<Card> communityCards) {
        List<Card> allCards = new ArrayList<>(cards);
        allCards.addAll(communityCards);

        Collections.sort(allCards, new CardComparator());

        if (isRoyalFlush(allCards)) {
            return 1;
        } else if (isStraightFlush(allCards)) {
            return 2;
        } else if (isFourOfAKind(allCards)) {
            return 3;
        } else if (isFullHouse(allCards)) {
            return 4;
        } else if (isFlush(allCards)) {
            return 5;
        } else if (isStraight(allCards)) {
            return 6;
        } else if (isThreeOfAKind(allCards)) {
            return 7;
        } else if (isTwoPairs(allCards)) {
            return 8;
        } else if (isPair(allCards)) {
            return 9;
        } else {
            return 10; // High card
        }
    }

    public Card getCard(){
        return (Card) this.cards;
    }

    // Check if it's a royal flush
    private boolean isRoyalFlush(List<Card> cards) {
        return isStraightFlush(cards) && cards.get(4).getRank() == Rank.ACE;
    }

    // Check if it's a straight flush
    private boolean isStraightFlush(List<Card> cards) {
        return isFlush(cards) && isStraight(cards);
    }

    // Check if it's a four of a kind
    private boolean isFourOfAKind(List<Card> cards) {
        Map<Rank, Long> rankCounts = cards.stream()
                .collect(Collectors.groupingBy(Card::getRank, Collectors.counting()));

        return rankCounts.containsValue(4L);
    }

    // Check if it's a full house
    private boolean isFullHouse(List<Card> cards) {
        Map<Rank, Long> rankCounts = cards.stream()
                .collect(Collectors.groupingBy(Card::getRank, Collectors.counting()));

        return rankCounts.containsValue(3L) && rankCounts.containsValue(2L);
    }

    // Check if it's a flush
    private boolean isFlush(List<Card> cards) {
        Suit suit = cards.get(0).getSuit();
        return cards.stream().allMatch(card -> card.getSuit() == suit);
    }

    // Check if it's a straight
    private boolean isStraight(List<Card> cards) {
        for (int i = 1; i < cards.size(); i++) {
            if (cards.get(i).getRank().getValue() - cards.get(i - 1).getRank().getValue() != 1) {
                return false;
            }
        }
        return true;
    }

    // Check if it's a three of a kind
    private boolean isThreeOfAKind(List<Card> cards) {
        Map<Rank, Long> rankCounts = cards.stream()
                .collect(Collectors.groupingBy(Card::getRank, Collectors.counting()));

        return rankCounts.containsValue(3L);
    }

    // Check if it's two pairs
    private boolean isTwoPairs(List<Card> cards) {
        Map<Rank, Long> rankCounts = cards.stream()
                .collect(Collectors.groupingBy(Card::getRank, Collectors.counting()));

        return rankCounts.values().stream().filter(count -> count == 2).count() == 2;
    }

    // Check if it's a pair
    private boolean isPair(List<Card> cards) {
        Map<Rank, Long> rankCounts = cards.stream()
                .collect(Collectors.groupingBy(Card::getRank, Collectors.counting()));

        return rankCounts.containsValue(2L);
    }

    private class CardComparator implements Comparator<Card> {
        @Override
        public int compare(Card card1, Card card2) {
            return card1.getRank().getValue() - card2.getRank().getValue();
        }
    }
}
