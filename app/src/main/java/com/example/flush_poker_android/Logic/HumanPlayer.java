package com.example.flush_poker_android.Logic;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

public class HumanPlayer extends Hand implements Runnable, Player, Serializable {
    private final String name;
    private int currentBet;
    private String playerAction;
    private Set<String> availableActions;
    private Boolean hasFold = false;
    private Boolean actionIsDone = false;
    private int chips;
    private GameController controller;
    private Handler handlerUi;
    private Context context;
    public HumanPlayer(String name, int chips, Handler handler, Context context) {
        super(); // Hand, parent's class.
        this.name = name;
        this.chips = chips;
        this.playerAction = "";
        this.availableActions = new LinkedHashSet<>();
        this.handlerUi = handler;
        this.context = context;
    }
    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                try {
                    this.wait(); // Wait for the controller to signal your turn
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Perform actions for your turn
                makeDecision();

                synchronized (controller){
                    // Signal the controller that your turn is done
                    actionIsDone = true;
                    controller.notify();
                }
            }

        }
    }
    private void makeDecision() {
        // This is a basic AI logic for automatic decision-making.
        // You can make it more sophisticated based on your game rules.
        int currentBet = controller.getCurrentBet();
        int minRaise = currentBet - getCurrentBet() + 1;
        int maxRaise = getChips();

        Random random = new Random();
        int decision = random.nextInt(4); // Generate a random decision (0 to 3)

        if (decision == 0) {
            // 25% chance to fold
            fold();
            setPlayerAction("Fold");
        } else if (decision == 1) {
            // 25% chance to check
            if (getCurrentBet() == currentBet) {
                check();
                setPlayerAction("Check");
            }
        } else if (decision == 2) {
            // 25% chance to call
            int callAmount = currentBet - getCurrentBet();
            if (callAmount > 0 && callAmount <= getChips()) {
                bet(callAmount);
                setPlayerAction("Call");
            }
        } else {
            // 25% chance to raise
            int raiseAmount = random.nextInt(maxRaise - minRaise + 1) + minRaise;
            raise(raiseAmount);
            setPlayerAction("Raise");
        }

        // Signal that the AI has completed its action
        actionIsDone = true;
    }
    @Override
    public void setController(GameController controller){
        this.controller = controller;
    }

    private void updateUIToast(String str){
        handlerUi.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getPlayerChoice() {
    }
    @Override
    public String getPlayerAction(){
        return this.playerAction;
    }
    @Override
    public Boolean hasFolded(){
        return hasFold;
    }
    @Override
    public void setAvailableActions(int currentBet) {
        Set<String> actions = new LinkedHashSet<>();

        // Check if the player can Fold
        if (!this.hasFolded()) {
            actions.add("Fold");
        }
        // Check if the player can Check (bet 0 if no one has raised)
        if (this.getCurrentBet() == currentBet) {
            actions.add("Check");
        }

        // Check if the player can Call (match the current bet)
        int callAmount = currentBet - this.getCurrentBet();
        if (callAmount > 0 && callAmount <= this.getChips()) {
            actions.add("Call");
        }

        // Check if the player can Raise (if they have enough chips)
        int minimumRaise = currentBet - this.getCurrentBet() + 1; // Minimum raise amount
        int maximumRaise = this.getChips(); // Maximum raise is their chip stack
        if (currentBet > 0 && minimumRaise <= maximumRaise) {
            actions.add("Raise");
        }

        this.availableActions = actions;
    }
    @Override
    public void check() {
        // Implement checking logic
        // In poker, checking means that the player does not want to bet but wants to stay in the game.
        // You need to implement the logic to determine if a check is allowed based on the game state.
    }
    @Override
    public void bet(int amount) {
        // Implement betting logic
        // This method should handle the player placing a bet with the specified amount.
        // Ensure that the player has enough credits to make the bet, and deduct the amount from their credits.
        if(chips > amount)
            this.chips -= amount;
    }
    @Override
    public void fold() {
        // Implement folding logic
        // Folding means the player chooses to forfeit the current hand and not participate further.
        // You should update the game state to reflect that this player has folded.
        this.hasFold = true;
    }
    @Override
    public void raise(int amount){
        int total = currentBet + amount;
        bet(total);
    }
    @Override
    public void setCurrentBet(int bet){
        this.currentBet = bet;
    }
    @Override
    public void addToChipCount(int pot) {
        this.chips += pot;
    }
    @Override
    public int getChips() {
        return this.chips;
    }
    @Override
    public int getCurrentBet() {
        return this.currentBet;
    }
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public boolean actionIsDone() {
        return this.actionIsDone;
    }
    @Override
    public void setPlayerAction(String action) {
        this.playerAction = action;
    }
    @Override
    public void decreaseChips(int amount) {
        this.chips -= amount;
    }
    @Override
    public void setActionIsDone(boolean b) {
        this.actionIsDone = b;
    }
}

