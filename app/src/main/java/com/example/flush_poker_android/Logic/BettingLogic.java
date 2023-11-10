package com.example.flush_poker_android.Logic;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class BettingLogic {
    private final List<Player> players;
    private int currentBet;
    private int pot;

    public BettingLogic(int currentBet, int pot, List<Player> players) {
        this.players = players;
        this.currentBet = currentBet;
        this.pot = pot;
    }

    public void performBettingRound(int currentPlayerIndex) {
        // Continue the betting round until all players have acted.
        if (!checkBettingRoundComplete()) {
            // Get the current player in a circular manner.
            Player currentPlayer = players.get(currentPlayerIndex);

            // Get Available Actions
            currentPlayer.setAvailableActions(currentPlayer, currentBet);

            // Display the available actions to the player and prompt for their choice.
            String playerChoice = currentPlayer.getPlayerAction();

            // Handle the player's choice.
            if (playerChoice.equals("Fold")) {
                // Player folds and is out of the hand.
                currentPlayer.fold();
                players.remove(currentPlayer);
            } else if (playerChoice.equals("Call")) {
                // Player calls the current bet.
                int callAmount = currentBet - currentPlayer.getCurrentBet();
                currentPlayer.bet(callAmount);
                pot += callAmount;
            } else if (playerChoice.equals("Raise")) {
                // Player raises the current bet.
                int raiseAmount = promptPlayerForRaiseAmount(currentPlayer, currentBet);
                currentPlayer.bet(raiseAmount + currentBet);
                currentBet += raiseAmount;
                pot += currentBet;
            }

            // Move to the next player's turn in a circular manner.
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }

        // Betting round is complete.
        // Perform any additional logic, such as showing community cards, determining winners, and resetting the current bet.
    }


    public boolean checkBettingRoundComplete() {
        // Implement the logic to check if the betting round is complete.
        // You can iterate through the players and check if they have all matched the current bet.
        // Return true if the round is complete; otherwise, return false.
        for (Player player : players) {
            if (player.getChips() > 0 && !player.hasFolded() && player.getCurrentBet() < currentBet) {
                return false;
            }
        }
        return true;
    }


    private int promptPlayerForRaiseAmount(Player player, int currentBet) {
        // Implement the logic for prompting the player to enter the raise amount.
        // You can use a scanner or a user interface here to get the raise amount.
        // Return the chosen raise amount as an int.
        // For example, you can use a Scanner:
        // Scanner scanner = new Scanner(System.in);
        // System.out.print("Enter the raise amount: ");
        // int raiseAmount = scanner.nextInt();
        // return raiseAmount;
        return 0;
    }

    private boolean checkBettingRoundComplete(List<Player> players, int currentBet) {
        // Implement the logic to check if the betting round is complete.
        // You can iterate through the players and check if they have all matched the current bet.
        // Return true if the round is complete; otherwise, return false.
        // For example:
        for (Player player : players) {
            if (player.getChips() > 0 && !player.hasFolded() && player.getCurrentBet() < currentBet) {
                return false;
            }
        }
        return true;
    }
}

