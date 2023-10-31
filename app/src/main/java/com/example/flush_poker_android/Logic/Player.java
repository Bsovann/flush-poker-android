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
    }

    public void bet(int amount) {
        // Implement betting logic
    }

    public void fold() {
        // Implement folding logic
    }

    public void raise(int amount) {
        // Implement raising logic
    }

    public void setCurrentBet(int bet){
        this.currentBet = bet;
    }
}

