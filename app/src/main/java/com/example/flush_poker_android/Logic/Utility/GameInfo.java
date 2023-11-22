package com.example.flush_poker_android.Logic.Utility;
import com.example.flush_poker_android.Logic.BotPlayer;
import java.io.Serializable;
import java.util.List;

public class GameInfo implements Serializable {
    private List<Integer> communityCardIds;
    private int currentPlayerIndex;
    private int currentBet;
    private int pot;
    private BotPlayer winner;
    private List<BotPlayer> remainingPlayers;
    private boolean isGameActive;

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

    public void setWinner(BotPlayer winner) {
        this.winner = winner;
    }

    public void setRemainingPlayers(List<BotPlayer> remainingPlayers) {
        this.remainingPlayers = remainingPlayers;
    }

    public void setGameActive(boolean gameActive) {
        isGameActive = gameActive;
    }

    // Constructors
    public GameInfo() {

        this.communityCardIds = communityCardIds;
        this.currentPlayerIndex = currentPlayerIndex;
        this.currentBet = currentBet;
        this.pot = pot;
        this.winner = winner;
        this.remainingPlayers = remainingPlayers;
        this.isGameActive = isGameActive;
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

    public BotPlayer getWinner() {
        return winner;
    }

    public List<BotPlayer> getRemainingPlayers() {
        return (List<BotPlayer>)remainingPlayers;
    }

    public boolean isGameActive() {
        return isGameActive;
    }

    public List<Integer> getCommunityCardIds(){
        return this.communityCardIds;
    }
}
