package com.example.flush_poker_android.Logic;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;

import com.example.flush_poker_android.Logic.Utility.GameUpdateMessage;
import com.example.flush_poker_android.Logic.Utility.PlayerActionMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientPlayer extends Hand implements Player, Runnable, Serializable{
    private final String name;
    private int currentBet;
    private String playerAction;
    private List<String> availableActions;
    private Boolean hasFold = false;
    private Boolean actionIsDone = false;
    private int chips;
    private Handler handlerUi;
    private Context context;
    private int betAmount = 0;
    private boolean isTimeOut = false;
    private PlayerActionListener actionListener;

    // Network communication variables
    private Socket clientSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String server_ip;
    private static int server_port = 9898;
    private boolean isGameActive = false;
    private WifiP2pDevice hostDeviceInfo;

    public ClientPlayer(WifiP2pDevice hostDeviceInfo, String name, int chips, Handler handler, Context context) {
        super(); // Hand, parent's class.
        this.name = name;
        this.chips = chips;
        this.playerAction = "";
        this.availableActions = new ArrayList<>();
        this.handlerUi = handler;
        this.context = context;
        this.hostDeviceInfo = hostDeviceInfo;
    }

    @Override
    public void run() {
        // Connect to server
        connectToServer();
        if(clientSocket != null) {
            clientListener();
            startGame();
        } else{
            // Got rejected / Interrupted
            // Notify to PeerActivity
            // PeerActivity: tell user to try again!
        }
    }

    private void startGame() {
        while (!isGameActive) {
            synchronized (this) {

                // Is Player Turn
                try {
                    this.wait(); // Wait for the player to perform an action via UI
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Your turn is over, notify the GameController
                if(playerAction.isEmpty()){
                    this.hasFold = true;
                    this.playerAction = "Fold";
                    this.actionIsDone = true;
                    // Send Message to notify Server
                }
            }
        }
    }

    private void clientListener(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // Listen for messages from the server and update player state
                while (isGameActive) {
                    // Example: Listen for messages and handle them
                    Object message = receiveMessage();
                    if (message instanceof GameUpdateMessage) {
                        // Update player state based on message
                    }
                }
            }
        });
    }
    private void connectToServer() {
        try {
            // Assuming server IP and port are known
            clientSocket = new Socket(server_ip, server_port);
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object receiveMessage() {
        try {
            return inputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendMessage(Object message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fold() {
        sendMessage(new PlayerActionMessage("Fold", this.name));
        this.playerAction = "Fold";
        this.hasFold = true;
        this.actionIsDone = true;
    }
    @Override
    public void setActionListener(PlayerActionListener listener) {
        this.actionListener = listener;
    }
    @Override
    public List<String> getAvailableActions() {
        return this.availableActions;
    }
    @Override
    public void setController(PracticeModeGameController controller){
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
    public void makeAutoDecision() {
        // This is a basic AI logic for automatic decision-making.
        // You can make it more sophisticated based on your game rules.

        int minRaise = this.currentBet + 1;
        int maxRaise = this.chips;

        Random random = new Random();
        int decision = random.nextInt(availableActions.size()); // Generate a random decision (0 to 3)

        String playerAction = availableActions.get(decision);

        if (playerAction.equals("Fold")) {
            // 25% chance to fold
            fold();
            setPlayerAction("Fold");
        } else if (playerAction.equals("Check")) {
            // 25% chance to check
            check();
            setPlayerAction("Check");
        } else if (playerAction.equals("Call")) {
            // 25% chance to call
            bet(currentBet);
            setPlayerAction("Call");
        } else {
            // 25% chance to raise
            int raiseAmount = random.nextInt(maxRaise - minRaise + 1) + currentBet;
            bet(raiseAmount);
            setPlayerAction("Raise");
        }

        // Signal that the AI has completed its action
        actionIsDone = true;
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

