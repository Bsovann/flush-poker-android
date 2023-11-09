package com.example.flush_poker_android.Logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private boolean isRoyalFlush(List<Card> cards) {
        if (cards.size() < 5) {
            return false;
        }
        return isStraightFlush(cards) && cards.get(6).getRank() == Rank.ACE;
    }

    private boolean isStraightFlush(List<Card> cards) {
        if (cards.size() < 5) {
            return false;
        }
        return isFlush(cards) && isStraight(cards);
    }

    private boolean isFourOfAKind(List<Card> cards) {
        Map<Rank, Long> rankCounts = cards.stream()
                .collect(Collectors.groupingBy(Card::getRank, Collectors.counting()));
        return rankCounts.containsValue(4L);
    }

    private boolean isFullHouse(List<Card> cards) {
        Map<Rank, Long> rankCounts = cards.stream()
                .collect(Collectors.groupingBy(Card::getRank, Collectors.counting()));
        return rankCounts.containsValue(3L) && rankCounts.containsValue(2L);
    }

//    private boolean isFlush(List<Card> cards) {
//        Suit suit = cards.get(0).getSuit();
//        int count = 0;
//        return cards.stream().allMatch(card -> card.getSuit() == suit);
//    }
private boolean isFlush(List<Card> cards) {
    Map<Suit, Integer> suitCounts = new HashMap<>();

    for (Card card : cards) {
        Suit suit = card.getSuit();
        suitCounts.put(suit, suitCounts.getOrDefault(suit, 0) + 1);
    }

    return suitCounts.values().stream().anyMatch(count -> count >= 5);
}

    private boolean isStraight(List<Card> cards) {
        for (int i = 1; i < cards.size(); i++) {
            if (cards.get(i).getRank().getValue() - cards.get(i - 1).getRank().getValue() != 1) {
                return false;
            }
        }
        return true;
    }

    private boolean isThreeOfAKind(List<Card> cards) {
        Map<Rank, Long> rankCounts = cards.stream()
                .collect(Collectors.groupingBy(Card::getRank, Collectors.counting()));
        return rankCounts.containsValue(3L);
    }

    private boolean isTwoPairs(List<Card> cards) {
        Map<Rank, Long> rankCounts = cards.stream()
                .collect(Collectors.groupingBy(Card::getRank, Collectors.counting()));
        return rankCounts.values().stream().filter(count -> count == 2).count() == 2;
    }

    private boolean isPair(List<Card> cards) {
        Map<Rank, Long> rankCounts = cards.stream()
                .collect(Collectors.groupingBy(Card::getRank, Collectors.counting()));
        return rankCounts.containsValue(2L);
    }

    public void clearHand() {
        this.cards.clear();
    }

    private class CardComparator implements Comparator<Card> {
        @Override
        public int compare(Card card1, Card card2) {
            return card1.getRank().getValue() - card2.getRank().getValue();
        }
    }

    public List<Card> getCards() {
        return this.cards;
    }
}
