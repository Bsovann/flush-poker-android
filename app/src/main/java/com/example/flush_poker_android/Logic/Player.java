package com.example.flush_poker_android.Logic;

public class Player extends Hand {
    private String name;
    private int currentBet;
    private int credits;
    public Player(String name, int credits) {
        super();
        this.name = name;
        this.credits = credits;
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
    }

    public void fold() {
        // Implement folding logic
        // Folding means the player chooses to forfeit the current hand and not participate further.
        // You should update the game state to reflect that this player has folded.
    }

    public void raise(int amount) {
        // Implement raising logic
        // Raising means the player increases the current bet by the specified amount.
        // Make sure the player has enough credits to raise, and update the current bet accordingly.
    }

    public void setCurrentBet(int bet){
        this.currentBet = bet;
    }
}

