package com.example.flush_poker_android.Logic;

import android.content.Context;
import android.os.Handler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**Human Player class for Single Player. No networking capabilities.
 *
 * @author Bondith Sovann*/
public class PracticeModePlayer extends Hand implements Player, Runnable, Serializable{
    private final String name;
    private int currentBet;
    private String playerAction;
    private List<String> availableActions;
    private Boolean hasFold = false;
    private Boolean actionIsDone = false;
    private int chips;
    private PracticeModeGameController controller;
    private Handler handlerUi;
    private Context context;
    private int betAmount = 0;
    private boolean isTimeOut = false;

    public PracticeModePlayer(String name, int chips, Handler handler, Context context) {
        super(); // Hand, parent's class.
        this.name = name;
        this.chips = chips;
        this.playerAction = "";
        this.availableActions = new ArrayList<>();
        this.handlerUi = handler;
        this.context = context;
    }
    private PlayerActionListener actionListener;

    @Override
    public void setActionListener(PlayerActionListener listener) {
        this.actionListener = listener;
    }

    @Override
    public List<String> getAvailableActions() {
        return this.availableActions;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    this.wait(10000); // Wait for the player to perform an action via UI
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Your turn is over, notify the GameController
                synchronized (controller){
                    if(playerAction.isEmpty()){
                        this.hasFold = true;
                        this.playerAction = "Fold";
                        this.actionIsDone = true;
                    }
                    controller.notify();
                }
            }
        }
    }

    @Override
    public void setController(PracticeModeGameController controller){
        this.controller = controller;
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
        List<String> actions = new ArrayList<>();
        this.currentBet = currentBet;

        // Check if the player can Fold
        if (hasFold == false) {
            actions.add("Fold");
        }
        // Check if the player can Check (bet 0 if no one has raised)
        if (this.currentBet == 0) {
            actions.add("Check");
        }

        // Check if the player can Call (match the current bet)
        if (this.currentBet > 0 && this.currentBet <= this.chips) {
            actions.add("Call");
        }

        // Check if the player can Raise (if they have enough chips)
        int minimumRaise = this.currentBet + 1; // Minimum raise amount
        int maximumRaise = this.chips; // Maximum raise is their chip stack
        if (this.currentBet > 0 && minimumRaise <= maximumRaise) {
            actions.add("Raise");
        }
        this.availableActions = actions;
    }
    @Override
    public int getBetAmount() {
        return betAmount;
    }
    public void playerStateReset(){
        this.playerAction = "";
        this.actionIsDone = false;
        this.availableActions.clear();
        this.hasFold = false;
        this.clearHand();
    }
    @Override
    public void check() {
        // Implement checking logic
        // In poker, checking means that the player does not want to bet but wants to stay in the game.
        // You need to implement the logic to determine if a check is allowed based on the game state.
        this.betAmount = 0;
        this.actionIsDone = true;
    }

    @Override
    public void call() {
        // Implement checking logic
        // In poker, checking means that the player does not want to bet but wants to stay in the game.
        // You need to implement the logic to determine if a check is allowed based on the game state.
        bet(this.currentBet);
        this.actionIsDone = true;
    }

    @Override
    public void setHasFold(boolean b) {
        this.hasFold = b;
    }

    @Override
    public void bet(int amount) {
        // Implement betting logic
        // This method should handle the player placing a bet with the specified amount.
        // Ensure that the player has enough credits to make the bet, and deduct the amount from their credits.
        if(chips >= amount) {
            this.chips -= amount;
            this.betAmount = amount;
            this.actionIsDone = true;
        }
    }
    @Override
    public void fold() {
        // Implement folding logic
        // Folding means the player chooses to forfeit the current hand and not participate further.
        // You should update the game state to reflect that this player has folded.
        this.playerAction = "Fold";
        this.hasFold = true;
        this.actionIsDone = true;
    }
    @Override
    public void raise(int amount){
        int total = currentBet + amount;
        bet(total);
        this.actionIsDone = true;
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

    @Override
    public int compareHands(List<Card> communityCards) {
        return super.compareHands(communityCards);
    }


}

