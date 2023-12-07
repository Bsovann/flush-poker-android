/*
 * Author: Bondith Sovann
 * Description: This class represents the PeerActivity for the Flush Poker Android game.
 * It handles the game's UI and logic, including player actions, card rendering, and game state updates.
 * (For Multiplayer Mode)
 */
package com.example.flush_poker_android.Client;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flush_poker_android.Client.customviews.CardAdapter;
import com.example.flush_poker_android.Client.customviews.PlayerCountdownView;
import com.example.flush_poker_android.Logic.BotPlayer;
import com.example.flush_poker_android.Logic.ControllerPlayer;
import com.example.flush_poker_android.Logic.Player;
import com.example.flush_poker_android.Logic.PracticeModeGameController;
import com.example.flush_poker_android.Logic.Utility.CardUtils;
import com.example.flush_poker_android.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PeerActivity extends AppCompatActivity implements GameUpdateListener{

    private Dialog dialog;
    private SeekBar brightnessSeekBar;
    private float screenBrightness = 127 / 255.0f;
    List<GridView> playerViews;
    List<CardAdapter> playerAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<Player> players;
    private int pot;
    private Thread controllerThread;
    private ExecutorService playerThreadPool;
    private PracticeModeGameController gameController;
    private CardFragment cardFragment;
    private List<Integer> communityCardIds = new ArrayList<>();
    private List<Player> remainPlayers;
    private int currentPlayerIndex;
    private int dealerPosition;
    private Player winner;
    private List<RelativeLayout> playerPositions = new ArrayList<>();
    private static final int COMMUNITY_CARDS_MSG = 1;
    private static final int REMAIN_PLAYERS_MSG = 2;
    private static final int DEALER_INDEX_MSG = 3;
    private static final int WINNER_MSG = 4;
    private static final int POT_MSG = 5;
    private static final int PLAYER_INDEX_MSG = 6;
    private static final int CURRENT_PLAYER_ACTION_MSG = 7;
    private static final int GAME_START_MSG = 8;
    private Player hostPlayer;
    PlayerCountdownView playerCountdownView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Check if the fragment is not already added to avoid duplication
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_card, CardFragment.class, null)
                    .commitNow();
        }
        initWork();
    }
    /*
     *  Author: Bondith Sovann
     * Description: Initializes various components and settings for the game.
     * It sets up the UI, creates player objects, and starts the game controller thread.
     */
    private void initWork(){

        // Enable immersive mode
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Set screen orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_game);

        // Set up work
        dialog = new Dialog(this);
        playerViews = new ArrayList<>(5);
        playerAdapter = new ArrayList<>(5);
        players = new ArrayList<>();

        // Default info rendering
        renderImagesTemp();
        setupPlayerPositions();

        this.playerThreadPool = Executors.newFixedThreadPool(5);
        // Assign Each player to each Thread
        hostPlayer = new ControllerPlayer("Bondith",9000, handler, getApplicationContext());
        players.add(hostPlayer);
        for(int i = 1; i < 5; i++){
            players.add(new BotPlayer(
                    "Player "+ i, 9000, handler, getApplicationContext()));
        }

        // Listening to GameController if anything update.
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            //Background work here
            handleMessage();
        });

        // Instantiate GameController
        this.gameController = new PracticeModeGameController(
                players,
                handler,
                getApplicationContext(),
                playerThreadPool);

        // Set Controller to every player and launch Thread
        for(Player player : players) {
            player.setController(gameController);
            playerThreadPool.submit((Runnable) player);
        }

        // Render player info.
        renderPlayerInfo();

        // Instantiate controllerThread and start it.
        this.controllerThread = new Thread(gameController);
        controllerThread.start();
    }

    /*
     * Author: Bondith Sovann
     * Description: Renders the player's own cards in the UI.
     */
    private void renderMyCards() {
        GridView myCards = playerViews.get(0);
        List<Integer> cardIds = hostPlayer.getHand().stream().map(
                card -> CardUtils.getCardImageResourceId(card.toString(),
                        getApplicationContext())).collect(Collectors.toList());
        myCards.setAdapter(new CardAdapter(this, cardIds));
    }

    /*
     * Author: Bondith Sovann
     * Description: Handles the onResume event of the activity.
     */
    @Override
    public void onResume() {
        super.onResume();
        cardFragment = (CardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_card);

    }
    /*
     * Author: Bondith Sovann
     * Description: Handles the onDestroy event of the activity.
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            controllerThread.join();
            playerThreadPool.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    /*
     * Author: Bondith Sovann
     * Description: Handles the onPause event of the activity.
     */
    @Override
    protected void onPause(){
        super.onPause();
    }

    /*
     * Author: Bondith Sovann
     * Description: Handles the handleMessage event for processing updates from the game controller.
     */
    public void handleMessage() {
        // Create a handler for the UI thread
        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // Process data received from the background thread
                try {
                    if(msg.what == GAME_START_MSG){
                        renderMyCards();
                    }
                    else if (msg.what == COMMUNITY_CARDS_MSG) {
                        Bundle bundle = msg.getData();
                        List<Integer> cards = (List<Integer>) bundle.getSerializable("data");
                        communityCardIds.addAll(cards);
                        onCommunityCardsUpdate(communityCardIds);
                    } else if (msg.what == REMAIN_PLAYERS_MSG) {
                        Bundle bundle = msg.getData();
                        remainPlayers = (List<Player>) bundle.getSerializable("data");
                    } else if (msg.what == DEALER_INDEX_MSG) {
                        Bundle bundle = msg.getData();
                        dealerPosition = (int) bundle.getSerializable("data");
                        onUpdatePlayerPosition();
                    } else if (msg.what == WINNER_MSG) {
                        Bundle bundle = msg.getData();
                        winner = (Player) bundle.getSerializable("data");
                        if (winner != null) {
                            if (communityCardIds.size() == 5)
                                revealHands();

                            updateWinnerTextView("Winner: " + winner.getName());
                            // Delay for a specified time before proceeding to reset the game state
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Code to reset the game state goes here
                                    cleanUp();
                                    Toast.makeText(PeerActivity.this, "New Game!", Toast.LENGTH_SHORT).show();
                                    synchronized (gameController) {
                                        gameController.notify();
                                    }
                                }
                            }, 15000);
                        }
                    } else if (msg.what == POT_MSG) {
                        Bundle bundle = msg.getData();
                        pot = (int) bundle.getSerializable("data");
                        // update current pot
                        onPotUpdate();
                    } else if (msg.what == PLAYER_INDEX_MSG) {
                        Bundle bundle = msg.getData();
                        currentPlayerIndex = (int) bundle.getSerializable("data");
                        onPlayerTurnUpdate();
                    } else if (msg.what == CURRENT_PLAYER_ACTION_MSG){
//                        Bundle bundle = msg.getData();
                        onPlayerActionUpdate();
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void updateWinnerTextView(String name) {
        TextView textview = findViewById(R.id.winner);
        textview.setText(name);
    }
    private void cleanUp() {
        renderImagesTemp();
        resetPlayerPositions();
        renderPlayerInfo();
        updateWinnerTextView("");
        turnOffAllActionButtons(hostPlayer.getAvailableActions());
        communityCardIds.clear();
        pot = 0;
        remainPlayers = null;
        winner = null;
    }
    private void renderPlayerInfo() {
        for(int i = 0; i < players.size(); i++) {
            Context context = getApplicationContext();
            Player player = players.get(i);
            int resourcePlayerName = context.getResources().getIdentifier(String.format("player%dName", i), "id", context.getPackageName());
            int resourcePlayerAction = context.getResources().getIdentifier(String.format("player%dAction", i), "id", context.getPackageName());
            TextView name = findViewById(resourcePlayerName);
            TextView action = findViewById(resourcePlayerAction);

            name.setText(player.getName());
            action.setText("");
        }
    }
    private void resetPlayerPositions() {
        for(RelativeLayout layout : playerPositions)
            layout.setVisibility(View.INVISIBLE);
    }
    private void renderImagesTemp(){
        playerViews.add(findViewById(R.id.myCards));
        playerViews.add(findViewById(R.id.player1Cards));
        playerViews.add(findViewById(R.id.player2Cards));
        playerViews.add(findViewById(R.id.player3Cards));
        playerViews.add(findViewById(R.id.player4Cards));

        // Add players cards images and render
        for(int i = 0; i < 5; i++) {
            playerAdapter.add(new CardAdapter(this, Arrays.asList(R.drawable.back_of_card1, R.drawable.back_of_card1)));
        }

        // Render card
        for(int i = 0; i < 5; i++) {
            playerViews.get(i).setAdapter(playerAdapter.get(i));
        }
    }
    private void revealHands(){
        for(int i = 0; i < remainPlayers.size(); i++) {
            GridView view = playerViews.get(players.indexOf(remainPlayers.get(i)));
            List<Integer> playerCardIds = remainPlayers.get(i).getHand()
                                        .stream().map(card -> CardUtils.getCardImageResourceId(card.toString(), getApplicationContext()))
                                        .collect(Collectors.toList());

            view.setAdapter(new CardAdapter(this, playerCardIds));
        }
    }
    private void onUpdatePlayerPosition() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 3; i++){
                    int posIndex = (dealerPosition + i) % playerPositions.size();
                    RelativeLayout layout = playerPositions.get(posIndex);
                    TextView textView = (TextView) layout.getChildAt(0);

                    if(posIndex == dealerPosition)
                        textView.setText("D");
                    else if(posIndex == dealerPosition + 1)
                        textView.setText("SB");
                    else
                        textView.setText("BB");

                    layout.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    @Override
    public void onPlayerTurnUpdate(){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if(players.get(currentPlayerIndex).getName().equals(hostPlayer.getName())) {
                    renderAvailableActionsButtons(hostPlayer.getAvailableActions());
                }
                String resourceName = "player"+ currentPlayerIndex + "Countdown";
                Context context = getApplicationContext();
                int resourceId = context.getResources().getIdentifier(resourceName, "id", context.getPackageName());

                playerCountdownView = findViewById(resourceId);
                playerCountdownView.setVisibility(View.VISIBLE);
                playerCountdownView.startCountdown(10000); // Start a 30-second countdown
            }
        });
    }
    @Override
    public void onPotUpdate(){
        TextView pot = findViewById(R.id.pot);
        pot.setText(String.format("Pot: %d$", this.pot));
    }
    @Override
    public void onCommunityCardsUpdate(List<Integer> updatedCardImages) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (cardFragment != null) {
                    cardFragment.onCommunityCardsUpdate(updatedCardImages);
                } else {
                    // Handle the case where cardFragment is null
                    Log.e("YourActivity", "cardFragment is null");
                }
            }
        });
    }
    private void onPlayerActionUpdate() {

        Context context = getApplicationContext();
        Player player = players.get(currentPlayerIndex);
        int resourcePlayerName = context.getResources().getIdentifier(String.format("player%dName", currentPlayerIndex), "id", context.getPackageName());
        int resourcePlayerAction = context.getResources().getIdentifier(String.format("player%dAction", currentPlayerIndex), "id", context.getPackageName());
        TextView name = findViewById(resourcePlayerName);
        TextView action = findViewById(resourcePlayerAction);

        name.setText(player.getName());
        action.setText(player.getPlayerAction());
    }
    public void onClickExitBtn(View view){
        Intent intent = new Intent(PeerActivity.this, MainActivity.class);
        startActivity(intent);
    }
    public void onClickFoldBtn(View view){
        handlePlayerAction("Fold");
    }
    public void onClickCheckBtn(View view){
        handlePlayerAction("Check");
    }
    public void onClickCallBtn(View view){
        handlePlayerAction("Call");
    }
    public void onClickBetBtn(View view){
        handlePlayerAction("Bet");
    }
    private void handlePlayerAction(String action) {
        Player currentPlayer = players.get(currentPlayerIndex);

        if(action.equals("Fold"))
            currentPlayer.fold();
        else if (action.equals("Check"))
            currentPlayer.check();
        else if (action.equals("Call"))
            currentPlayer.call();
        else if (action.equals("Bet"))
            currentPlayer.bet(currentPlayer.getCurrentBet() + 1000); // Just leave it static for now.
        else
            currentPlayer.raise(currentPlayer.getCurrentBet() * 2); // raise double for now.

        currentPlayer.setPlayerAction(action);
        playerCountdownView.setVisibility(View.INVISIBLE);
        turnOffAllActionButtons(currentPlayer.getAvailableActions());
        synchronized (currentPlayer) {
            currentPlayer.notify(); // Notify the HumanPlayer's thread
        }
    }
    public void onClickChatIcon(View view){
        // Chat Dialog
        dialog.setContentView(R.layout.chat_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        // Exit button
        Button sendChatBtn = dialog.findViewById(R.id.sendChatButton);
        sendChatBtn.setOnClickListener(x -> dialog.dismiss());
    }
    public void onClickSettingIcon(View view){

        // SettingDialog
        dialog.setContentView(R.layout.setting_dialog);
        brightnessSeekBar = dialog.findViewById(R.id.brightnessSeekBar);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        brightnessSeekBar.setProgress((int) (screenBrightness * 255));
        dialog.show();
        // Brightness control
        setupSeekBarListener();

        // Exit button
        Button closeSettingBtn = dialog.findViewById(R.id.closeSettingButton);
        closeSettingBtn.setOnClickListener(x -> dialog.dismiss());
    }
    public void onClickInstructionIcon(View view){
        // Instruction Dialog
        dialog.setContentView(R.layout.instruction_dialog);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        // Exit button
        Button closeInstructionBtn = dialog.findViewById(R.id.closeInstructionButton);
        closeInstructionBtn.setOnClickListener(x -> dialog.dismiss());
    }
    public void setupSeekBarListener(){
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int currentValue, boolean fromUser) {
                if(fromUser){
                    screenBrightness = currentValue / 255.0f;
                    WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                    layoutParams.screenBrightness = screenBrightness;
                    getWindow().setAttributes(layoutParams);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    private void setupPlayerPositions() {
        playerPositions.add(findViewById(R.id.posIconPlayer0Layout));
        playerPositions.add(findViewById(R.id.posIconPlayer1Layout));
        playerPositions.add(findViewById(R.id.posIconPlayer2Layout));
        playerPositions.add(findViewById(R.id.posIconPlayer3Layout));
        playerPositions.add(findViewById(R.id.posIconPlayer4Layout));
    }
    private void renderAvailableActionsButtons(List<String> actions){
        try{
            for(String action : actions) {
                Context context = getApplicationContext();
                int btnId = context.getResources().getIdentifier(String.format("%sBtn", action.toLowerCase()), "id", context.getPackageName());
                Button btn = findViewById(btnId);
                btn.setVisibility(View.VISIBLE);
            }
        } catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    private void turnOffAllActionButtons(List<String> availableActions) {
        try{
            for(String action : availableActions) {
                Context context = getApplicationContext();
                int btnId = context.getResources().getIdentifier(String.format("%sBtn", action.toLowerCase()), "id", context.getPackageName());
                Button btn = findViewById(btnId);
                btn.setVisibility(View.INVISIBLE);
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }
}

