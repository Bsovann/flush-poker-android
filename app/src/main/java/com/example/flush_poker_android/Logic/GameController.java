package com.example.flush_poker_android.Logic;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flush_poker_android.Logic.Utility.CardUtils;
import com.example.flush_poker_android.Logic.Utility.GameInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class GameController extends AppCompatActivity implements Runnable {
    List<Player> remainPlayers;
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
    private final Handler mainUiThread;
    private final Context gameContext;
    private ExecutorService playerThreadPool;

    private List<Integer> communityCardsId;
    private GameInfo gameInfo;
    private Semaphore objectLocker;

    public GameController(List<Player> players, Handler handler,
                          Context context,
                          ExecutorService playerThreadPool,
                          Semaphore objectLocker) {
        this.players = players;
        this.mainUiThread = handler;
        this.gameContext = context;
        this.playerThreadPool = playerThreadPool;
        this.objectLocker = objectLocker;

        initializeGame();
    }

    @Override
    public void run() {
        while (isGameActive()) {
            startGame();
        }
    }
    private void endGame() {
        playerThreadPool.shutdown();
        // Notify HostActivity
        // Shut Down Thread
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private void initializeGame() {
        this.deck = new Deck();
        this.communityCards = new ArrayList<>();
        this.dealerPosition = 0;
        this.pot = 0;
        this.remainPlayers = players;
    }
    public void startGame() {
        // Initialize gameInfo to send to activity
        this.gameInfo =  new GameInfo();

        // DeckShuffle
        deck.shuffle();

        // Initial Game State
        smallBlind = 10;
        bigBlind = 20;
        currentBet = bigBlind;

        dealer = players.get(dealerPosition);
        gameInfo.setDealerPosition(dealerPosition);
        notifyUpdateToActivity();

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
        updateCommunityCardsUI();

        // Start Betting Round!
        while (communityCards.size() != 5) {

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (isBettingRoundComplete()) {
                dealCommunityCards();
                updateCommunityCardsUI();
            }
            else {
                // Players can choose to fold, call, or raise.
                // Update the current bet, pot, and player actions accordingly.
                synchronized (currentPlayer) {

                    currentPlayer.setAvailableActions(currentBet);
                    if (!currentPlayer.hasFolded())
                        currentPlayer.notify();
                    else {
                        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
                        currentPlayer = players.get(currentPlayerIndex);
                        continue;
                    }
                }

                // Wait for the player's turn to be completed.
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                String playerChoice = currentPlayer.getPlayerAction();
                updateToastUi(currentPlayer.getName() + " " + playerChoice);

                // Process the player's action (e.g., update bets, check for folds, etc.)
                processPlayerChoice(playerChoice);
                updatePotUi();

                // Move to the next player's turn.
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
                currentPlayer = players.get(currentPlayerIndex);

                this.remainPlayers = players.stream().filter(player -> player.hasFolded() == false).collect(Collectors.toList());
                setRemainPlayers(); // set Remain players and compare hands.
                updateToastUi("Remain Players: " + remainPlayers.size());

                if (remainPlayers.size() == 1) {
                    break;
                }
            }
        }

        if(determineWinner()){
            notifyUpdateToActivity();
            updateToastUi("The Winner is " + winner.getName());
        }

        synchronized (this){
            try {
                this.wait();
                gameStateReset();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        updateToastUi("Game state has been reset!");
    }
    private void updatePotUi(){
        gameInfo.setPot(this.pot);
        notifyUpdateToActivity();
    }
    private void setRemainPlayers(){
        gameInfo.setRemainingPlayers(this.remainPlayers);
        notifyUpdateToActivity();
    }
    // Update Card UI
    private void updateCommunityCardsUI() {
        this.communityCardsId = communityCards.stream()
                .map(card -> CardUtils.getCardImageResourceId(card.toString(), gameContext))
                .collect(Collectors.toList());

        gameInfo.setCommunityCardIds(this.communityCardsId);
        notifyUpdateToActivity();
    }

    // Call this method when there's a card update
    private void notifyUpdateToActivity() {
        Message message = mainUiThread.obtainMessage();
        message.what = 1;

        Bundle bundle = new Bundle();
        bundle.putSerializable("dataObject", this.gameInfo);
        message.setData(bundle);
        mainUiThread.sendMessage(message);
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

    private void gameStateReset(){
        // Reset the game state for the next hand.
        for (Player player : players) {
            player.playerStateReset();
        }

        remainPlayers = players;
        communityCards.clear();
        communityCardsId.clear();
        deck.reset();
        deck.shuffle();
        dealerPosition = (dealerPosition + 1) % players.size();

        pot = 0;
        gameInfo.setPot(this.pot);
        gameInfo.setCommunityCardIds(this.communityCardsId);
        gameInfo.setRemainingPlayers(this.remainPlayers);
        gameInfo.setDealerPosition(this.dealerPosition);
        notifyUpdateToActivity();
    }
    private void postBlind(Player player, int blindAmount) {
        player.decreaseChips(blindAmount);
        pot += blindAmount;
        notifyUpdateToActivity();
    }
    private Boolean determineWinner() {
        if (communityCards.size() == 5 || remainPlayers.size() == 1) {
            this.winner = determineWinner(remainPlayers, communityCards);
            this.winner.addToChipCount(pot);
            this.gameInfo.setWinner(winner);
            return true;
        }
        return false;
    }
    private void processPlayerChoice(String playerChoice) {
        // Handle the player's choice.
        if (playerChoice.equals("Fold")) {
            // Player folds and is out of the hand.
            currentPlayer.fold();
//            players.remove(currentPlayer);
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
        } else if (playerChoice.equals("Check")) {
        }
    }
    public synchronized boolean isBettingRoundComplete() {
        // Implement the logic to check if the betting round is complete.
        // You can iterate through the players and check if they have all matched the current bet.
        // Return true if the round is complete; otherwise, return false.
        for (Player player : remainPlayers) {
            if (!player.actionIsDone()) {
                return false;
            }
        }
        for (Player player : remainPlayers)
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
        for(int i = 0; i < 2; i++) {
            for (Player player : remainPlayers)
                player.addCard(deck.dealCard());
        }
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
