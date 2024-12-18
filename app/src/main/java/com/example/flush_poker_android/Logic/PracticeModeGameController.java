package com.example.flush_poker_android.Logic;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flush_poker_android.Logic.Utility.CardUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**The PracticeModeGameController handles the flow of all game logic for Single Player.
 *
 * @author Bondith Sovann*/
public class PracticeModeGameController extends AppCompatActivity implements Runnable {
    List<Player> remainPlayers;
    private Deck deck;
    private final List<Player> players;
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
    private final ExecutorService playerThreadPool;
    private List<Integer> communityCardsId;
    private static final int COMMUNITY_CARDS_MSG = 1;
    private static final int REMAIN_PLAYERS_MSG = 2;
    private static final int DEALER_INDEX_MSG = 3;
    private static final int WINNER_MSG = 4;
    private static final int POT_MSG = 5;
    private static final int PLAYER_INDEX_MSG = 6;
    private static final int CURRENT_PLAYER_ACTION_MSG = 7;
    private static final int GAME_START_MSG = 8;
    private static final int CURRENT_BET_MSG = 9;


    public PracticeModeGameController(List<Player> players, Handler handler,
                                      Context context,
                                      ExecutorService playerThreadPool) {
        this.players = players;
        this.mainUiThread = handler;
        this.gameContext = context;
        this.playerThreadPool = playerThreadPool;
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
        // DeckShuffle
        deck.shuffle();

        // Initial Game State
        smallBlind = 10;
        bigBlind = 20;
        currentBet = bigBlind;
        notifyCurrentBetToActivity();

        dealer = players.get(dealerPosition);
        notifyDealerPositionUpdateToActivity();

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
        notifyGameStartToActivity();

        // Player, Right of Big Blind (Clockwise)
        currentPlayerIndex = (bigBlindPosition + 1) % players.size();
        currentPlayer = players.get(currentPlayerIndex);

        // Deal community cards when
        dealCommunityCards();

        // Start Betting Round!
        while (communityCards.size() != 5) {

            if (isBettingRoundComplete()) {
                currentBet = 0;
                notifyCurrentBetToActivity();
                dealCommunityCards();
            }
            else {
                notifyCurrentPlayerUpdateToActivity();
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
                // Process the player's action (e.g., update bets, check for folds, etc.)
                processPlayerChoice(playerChoice);
                notifyCurrentBetToActivity();
                notifyPotUpdateToActivity();
                notifyCurrentPlayerActionToActivity();

                // Move to the next player's turn.
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
                currentPlayer = players.get(currentPlayerIndex);

                this.remainPlayers = players.stream().filter(player -> !player.hasFolded()).collect(Collectors.toList());
                notifyRemainPlayersUpdateToActivity(); // set Remain players and compare hands.

                if (remainPlayers.size() == 1) {
                    break;
                }
            }
        }

        if(determineWinner()){
            notifyWinnerUpdateToActivity();
        }
        synchronized (this){
            try {
                this.wait();
                gameStateReset();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void notifyCurrentPlayerActionToActivity() {
        Message message = mainUiThread.obtainMessage();
        message.what = CURRENT_PLAYER_ACTION_MSG;

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", currentPlayer.getPlayerAction());
        message.setData(bundle);
        mainUiThread.sendMessage(message);
    }
    private void updateCommunityCardsUI(List<Card> images) {
        this.communityCardsId = images.stream()
                .map(card -> CardUtils.getCardImageResourceId(card.toString(), gameContext))
                .collect(Collectors.toList());
        notifyCommunityCardsUpdateToActivity(communityCardsId);
    }
    private void notifyCommunityCardsUpdateToActivity(List<Integer> cardIds) {
        Message message = mainUiThread.obtainMessage();
        message.what = COMMUNITY_CARDS_MSG;

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", (Serializable) cardIds);
        message.setData(bundle);
        mainUiThread.sendMessage(message);
    }
    private void notifyCurrentBetToActivity() {
        Message message = mainUiThread.obtainMessage();
        message.what = CURRENT_BET_MSG;
        Bundle bundle = new Bundle();
        bundle.putSerializable("data", this.currentBet);
        message.setData(bundle);
        mainUiThread.sendMessage(message);
    }
    private void notifyPotUpdateToActivity() {
        Message message = mainUiThread.obtainMessage();
        message.what = POT_MSG;

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", this.pot);
        message.setData(bundle);
        mainUiThread.sendMessage(message);
    }
    private void notifyWinnerUpdateToActivity() {
        Message message = mainUiThread.obtainMessage();
        message.what = WINNER_MSG;
        Bundle bundle = new Bundle();
        bundle.putSerializable("data", (Serializable) this.winner);
        message.setData(bundle);
        mainUiThread.sendMessage(message);
    }
    private void notifyRemainPlayersUpdateToActivity() {
        Message message = mainUiThread.obtainMessage();
        message.what = REMAIN_PLAYERS_MSG;
        Bundle bundle = new Bundle();
        bundle.putSerializable("data", (Serializable) this.remainPlayers);
        message.setData(bundle);
        mainUiThread.sendMessage(message);
    }
    private void notifyCurrentPlayerUpdateToActivity() {
        Message message = mainUiThread.obtainMessage();
        message.what = PLAYER_INDEX_MSG;

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", this.currentPlayerIndex);
        message.setData(bundle);
        mainUiThread.sendMessage(message);
    }
    private void notifyDealerPositionUpdateToActivity() {
        Message message = mainUiThread.obtainMessage();
        message.what = DEALER_INDEX_MSG;

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", this.dealerPosition);
        message.setData(bundle);
        mainUiThread.sendMessage(message);
    }
    private void notifyGameStartToActivity() {
        Message message = mainUiThread.obtainMessage();
        message.what = GAME_START_MSG;
        mainUiThread.sendMessage(message);
    }
    public synchronized boolean isGameActive() {
        // Check if the game is active
        return players.size() > 1;
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
    }
    private void postBlind(Player player, int blindAmount) {
        player.decreaseChips(blindAmount);
        pot += blindAmount;
        notifyPotUpdateToActivity();
    }
    private Boolean determineWinner() {
        if (communityCards.size() == 5 || remainPlayers.size() == 1) {
            this.winner = determineWinner(remainPlayers, communityCards);
            this.winner.addToChipCount(pot);
            return true;
        }
        return false;
    }
    private void processPlayerChoice(String playerChoice) {
        // Handle the player's choice.
        int betAmount = currentPlayer.getBetAmount();
        if (playerChoice.equals("Fold")) {
            // Player folds and is out of the hand.
            currentPlayer.fold();
        } else if (playerChoice.equals("Call")) {
            // Player calls the current bet.
            currentBet = betAmount;
            pot += betAmount;
        } else if (playerChoice.equals("Raise")) {
            currentBet = betAmount;
            pot += betAmount;
        } else if (playerChoice.equals("Check")) {
            currentBet = betAmount;
            pot += betAmount;
        }

    }
    public synchronized boolean isBettingRoundComplete() {
        for (Player player : remainPlayers) {
            if (!player.actionIsDone()) {
                return false;
            }
        }
        for (Player player : remainPlayers) {
            player.setActionIsDone(false);
            player.setPlayerAction("");
            player.setHasFold(false);
        }
        return true;
    }
    public synchronized void dealCommunityCards() {
        if (communityCards.size() < 3) {
            List<Card> cards = deck.draw(3);
            communityCards.addAll(cards);
            updateCommunityCardsUI(cards);
        } else if (communityCards.size() == 3) {
            List<Card> cards = deck.draw(1);
            communityCards.addAll(cards);
            updateCommunityCardsUI(cards);
        } else if (communityCards.size() == 4) {
            List<Card> cards = deck.draw(1);
            communityCards.addAll(cards);
            updateCommunityCardsUI(cards);
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
