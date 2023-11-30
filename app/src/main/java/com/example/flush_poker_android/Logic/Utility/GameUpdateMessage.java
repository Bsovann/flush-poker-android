package com.example.flush_poker_android.Logic.Utility;

import java.io.Serializable;
import java.util.List;

public class GameUpdateMessage implements Serializable {
    // You can include various fields that represent the game state
    private final int currentPot;
    private final List<String> communityCards;
    private final String currentPlayerTurn;
    private final int currentBet;
    // Add other fields as necessary

    // Constructor
    public GameUpdateMessage(int currentPot, List<String> communityCards, String currentPlayerTurn, int currentBet) {
        this.currentPot = currentPot;
        this.communityCards = communityCards;
        this.currentPlayerTurn = currentPlayerTurn;
        this.currentBet = currentBet;
    }

    // Getters
    public int getCurrentPot() {
        return currentPot;
    }

    public List<String> getCommunityCards() {
        return communityCards;
    }

    public String getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "GameUpdateMessage{" +
                "currentPot=" + currentPot +
                ", communityCards=" + communityCards +
                ", currentPlayerTurn='" + currentPlayerTurn + '\'' +
                ", minimumBet=" + currentBet +
                '}';
    }

    // Add other methods as necessary, like methods to check the game state
}

