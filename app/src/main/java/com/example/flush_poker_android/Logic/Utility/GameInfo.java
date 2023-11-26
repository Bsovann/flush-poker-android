package com.example.flush_poker_android.Logic.Utility;

import com.example.flush_poker_android.Logic.Player;

import java.io.Serializable;
import java.util.List;

public class GameInfo implements Serializable {
    private List<Integer> communityCardIds;
    private int currentPlayerIndex;
    private int currentBet;
    private int pot;
    private Player winner;
    private List<Player> remainingPlayers;
    private boolean isGameActive;

    public int getDealerPosition() {
        return dealerPosition;
    }

    public void setDealerPosition(int dealerPosition) {
        this.dealerPosition = dealerPosition;
    }

    private int dealerPosition;

    public void setCommunityCardIds(List<Integer> communityCardIds) {
        this.communityCardIds = communityCardIds;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public void setCurrentBet(int currentBet) {
        this.currentBet = currentBet;
    }

    public void setPot(int pot) {
        this.pot = pot;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public void setRemainingPlayers(List<Player> remainingPlayers) {
        this.remainingPlayers = remainingPlayers;
    }

    public void setGameActive(boolean gameActive) {
        isGameActive = gameActive;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public int getPot() {
        return pot;
    }

    public Player getWinner() {
        return winner;
    }

    public List<Player> getRemainingPlayers() {
        return (List<Player>)remainingPlayers;
    }

    public boolean isGameActive() {
        return isGameActive;
    }

    public List<Integer> getCommunityCardIds(){
        return this.communityCardIds;
    }
}
