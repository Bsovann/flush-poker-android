package com.example.flush_poker_android.Logic;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Player extends Hand {
    private final String name;
    private int currentBet;

    private final String playerAction;

    private List<String> availableActions;
    private Boolean hasFold = false;
    private int chips;
    public Player(String name, int chips) {
        super();
        this.name = name;
        this.chips = chips;
        this.playerAction = "";
        this.availableActions = new ArrayList<>();
    }

    public String getPlayerAction(){
        return this.playerAction;
    }

    public Boolean hasFolded(){
        return hasFold;
    }


    public void setAvailableActions(Player currentPlayer, int currentBet) {
        this.currentBet = currentBet;
        // Determine the available actions for the current player.
        List<String> availableActions = new ArrayList<>();
        availableActions.add("Fold");
        if (currentPlayer.getChips() > this.currentBet) {
            availableActions.add("Call");
            availableActions.add("Raise");
        }
        this.availableActions = availableActions;
    }

    public List<String> getAvailableActions(){
        return this.availableActions;
    }

    public void check() {
        // Implement checking logic
        // In poker, checking means that the player does not want to bet but wants to stay in the game.
        // You need to implement the logic to determine if a check is allowed based on the game state.
    }

    public void bet(int amount) {
        // Implement betting logic
        // This method should handle the player placing a bet with the specified amount.
        // Ensure that the player has enough credits to make the bet, and deduct the amount from their credits.
        if(chips > amount)
            this.chips -= amount;
    }

    public void fold() {
        // Implement folding logic
        // Folding means the player chooses to forfeit the current hand and not participate further.
        // You should update the game state to reflect that this player has folded.
        this.hasFold = true;
    }

    public void raise(int amount) {
        // Implement raising logic
        // Raising means the player increases the current bet by the specified amount.
        // Make sure the player has enough credits to raise, and update the current bet accordingly.
        int totalBet = currentBet + amount;
        if(chips > totalBet)
            this.chips -= totalBet;
    }

    public void setCurrentBet(int bet){
        this.currentBet = bet;
    }

    public void addToChipCount(int pot) {
        this.chips += pot;
    }

    public int getChips() {
        return this.chips;
    }

    public int getCurrentBet() {
        return this.currentBet;
    }
}

