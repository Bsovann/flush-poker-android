package com.example.flush_poker_android.Client.Utility;
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

    // Constructors
    public GameInfo(List<Integer> communityCardIds, int currentPlayerIndex,
                    int currentBet, int pot, BotPlayer winner,
                    List<BotPlayer> remainingPlayers, boolean isGameActive) {

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
        return remainingPlayers;
    }

    public boolean isGameActive() {
        return isGameActive;
    }

    public List<Integer> getCommunityCardIds(){
        return this.communityCardIds;
    }
}
