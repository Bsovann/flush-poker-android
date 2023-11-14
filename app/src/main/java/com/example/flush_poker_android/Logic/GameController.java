package com.example.flush_poker_android.Logic;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class GameController implements Runnable {
    private Deck deck;
    private List<Player> players;
    private List<Card> communityCards;
    private Player currentPlayer;
    private int dealerPosition;
    private int smallBlind;
    private int bigBlind;
    private int currentBet;
    private int pot;
    private Player dealer;
    private int currentPlayerIndex;
    private Player winner;
    private Handler mainUiThread;
    private Boolean gameActive = true;
    private Context gameContext;
    private Semaphore turn;
    private Player playerTurn;

    private List<Thread> playerThreads;


    public GameController(List<Player> players, Handler handler, Context context) {
        this.players = players;
        this.mainUiThread = handler;
        this.gameContext = context;
        this.playerThreads = new ArrayList<>();
        this.turn = new Semaphore(1);

        // Initialize game logic
        initializeGame();
    }

    public void setPlayersAndInitGame(List<Player> players){
        this.players = players;

    }

    @Override
    public void run() {
        while(this.players.size() > 1){
            startGame();
        }
        endGame();
    }
    private void setUpPlayersThread(){
        for(int i = 0; i < players.size(); i++){
            Player player = players.get(i);
                    player.setTurnLocker(turn);

            Thread thread = new Thread(player);
            thread.setName(player.getName());
            playerThreads.add(thread);
            thread.start();
        }
    }

    private void endGame() {
        this.gameActive = false;
    }

    private void initializeGame() {
        this.deck = new Deck();
        this.communityCards = new ArrayList<>();
        this.dealerPosition = 0;
        this.pot = 0;
    }

    public Player getPlayerTurn(){
        return this.playerTurn;
    }

    public void startGame() {

        // DeckShuffle
        deck.shuffle();

        // Initial Game State
        smallBlind = 10;
        bigBlind = 20;
        currentBet = bigBlind;
        dealer = players.get(dealerPosition);

        // Determine small blind and big blind positions
        int smallBlindPosition = (dealerPosition + 1) % players.size();
        int bigBlindPosition = (dealerPosition + 2) % players.size();

        // Player in small blind position posts the small blind
        Player smallBlindPlayer = players.get(smallBlindPosition);
        postBlind(smallBlindPlayer, smallBlind);

        // Player in big blind position posts the big blind
        Player bigBlindPlayer = players.get(bigBlindPosition);
        postBlind(bigBlindPlayer, bigBlind);

        // Initialize hands
        dealTwoHoleCardsToPlayers();

        // Player, Right of Big Blind (Clockwise)
        currentPlayerIndex = (bigBlindPosition + 1) % players.size();
        currentPlayer = players.get(currentPlayerIndex);

        // Deal community cards when
        dealCommunityCards();

        // Start Betting Round!
        while (true) {

            if(checkBettingRoundComplete())
                dealCommunityCards();

            // Players can choose to fold, call, or raise.
            // Update the current bet, pot, and player actions accordingly.
            currentPlayer.setAvailableActions(currentBet);
            synchronized (currentPlayer){
                currentPlayer.notify();
            }

            // Wait for the player's turn to be completed.
            while(!currentPlayer.actionIsDone()) {
                synchronized (this){
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            String playerChoice = currentPlayer.getPlayerAction();
            updateToastUi(currentPlayer.getName() + " " + playerChoice);

            // Process the player's action (e.g., update bets, check for folds, etc.)
            processPlayerChoice(playerChoice);

            updateToastUi(communityCards.toString());

            // Check for showdown and determine the winner.
            if(determineWinner()) {
                updateToastUi("The Winner is " + winner.getName());
                break;
            }
        }



    }

    public synchronized void startPlayerTurn(Player player) {
        currentPlayer = player;
        player.notifyAll(); // Signal the player that it's their turn
    }

    public synchronized void endPlayerTurn() {
        notifyAll(); // Signal that the player's turn is done
    }
    public synchronized boolean isGameActive() {
        // Check if the game is active
        return players.size() > 1;
    }

    private void updateToastUi(String playerChoice) {
        mainUiThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(gameContext, playerChoice, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postBlind(Player player, int blindAmount) {
        player.decreaseChips(blindAmount);
        pot += blindAmount;
        // You can add a log or notification to inform other players about the blind being posted.
    }
    private Boolean determineWinner() {
        if (communityCards.size() == 5) {
            this.winner = determineWinner(players, communityCards);
            this.winner.addToChipCount(pot);
            pot = 0;

            // Reset the game state for the next hand.
            for (Player player : players) {
                player.clearHand();
            }
            communityCards.clear();
            deck.reset();
            deck.shuffle();
            dealerPosition++;
            return true;
        } else {
            // Move to the next player's turn.
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            currentPlayer = players.get(currentPlayerIndex);
            return false;
        }
    }
    private void processPlayerChoice(String playerChoice) {
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
//            int raiseAmount = promptPlayerForRaiseAmount(currentPlayer, currentBet);
//            currentPlayer.bet(raiseAmount + currentBet);
//            currentBet += raiseAmount;
//            pot += currentBet;
        }
    }
    public boolean checkBettingRoundComplete() {
        // Implement the logic to check if the betting round is complete.
        // You can iterate through the players and check if they have all matched the current bet.
        // Return true if the round is complete; otherwise, return false.
        for (Player player : players) {
            if (!player.actionIsDone()) {
                return false;
            }
        }
        for (Player player : players)
            player.setActionIsDone(false);
        return true;
    }
    public synchronized void dealCommunityCards() {
        if (communityCards.size() < 3) {
            communityCards.addAll(deck.draw(3));
        } else if (communityCards.size() == 3) {
            communityCards.addAll(deck.draw(1));
        } else if (communityCards.size() == 4) {
            communityCards.addAll(deck.draw(1));
        }
    }
    public Player determineWinner(List<Player> players, List<Card> communityCards) {
        Player winningPlayer = null;
        int bestHandRank = -1;

        for (Player player : players) {
            int playerHandRank = player.compareHands(communityCards);

            if (playerHandRank > bestHandRank) {
                bestHandRank = playerHandRank;
                winningPlayer = player;
            }
        }

        return winningPlayer;
    }
    public List<Player> getPlayers() {
        return players;
    }
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    public int getPot() {
        return pot;
    }
    public synchronized void dealTwoHoleCardsToPlayers() {
        for (Player player : players)
            player.addCard(deck.dealCard());
    }
    public Player getWinner() {
        return this.winner;
    }
    public List getCommunityCards() {
        return communityCards;
    }
    public int getDealerPosition() {
        return this.dealerPosition;
    }
    public void incrementDealerPosition() {
        this.dealerPosition++;
    }
    public int getSmallBlind() {
        return this.smallBlind;
    }
    public int getBigBlind() {
        return this.bigBlind;
    }
    public int getCurrentBet() {
        return this.currentBet;
    }
    public Object getDealer() {
        return this.dealer;
    }

}
