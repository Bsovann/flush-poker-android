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

import androidx.appcompat.app.AppCompatActivity;

import com.example.flush_poker_android.Client.customviews.CardAdapter;
import com.example.flush_poker_android.Client.customviews.PlayerCountdownView;
import com.example.flush_poker_android.Logic.BotPlayer;
import com.example.flush_poker_android.Logic.GameController;
import com.example.flush_poker_android.Logic.Player;
import com.example.flush_poker_android.Logic.Utility.CardUtils;
import com.example.flush_poker_android.Logic.Utility.GameInfo;
import com.example.flush_poker_android.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class HostActivity extends AppCompatActivity implements GameUpdateListener {
    private Dialog dialog;
    private SeekBar brightnessSeekBar;
    private float screenBrightness = 127 / 255.0f;
    GridView communityCardView;
    List<GridView> playerViews;
    List<CardAdapter> playerAdapter;
    CardAdapter commnityCardAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<Player> players;
    private int pot;
    private Thread controllerThread;
    private ExecutorService playerThreadPool;
    private GameController gameController;
    private CardFragment cardFragment;
    private List<Integer> communityCardIds = new ArrayList<>();
    private List<Player> remainPlayers;
    private int currentPlayerIndex;
    private int dealerPosition;
    private Player winner;
    private Semaphore objectLocker = new Semaphore(0);

    private List<RelativeLayout> playerPositions = new ArrayList<>();
    GameInfo dataObject = null;

    private final int COMMUNITY_CARDS_MSG = 1;
    private final int REMAIN_PLAYERS_MSG = 2;
    private final int DEALER_INDEX_MSG = 3;
    private final int WINNER_MSG = 4;
    private final int POT_MSG = 5;
    private final int PLAYER_INDEX_MSG = 6;




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
    private long lastMessageTimestamp;
    public void handleMessage() {
        // Create a handler for the UI thread
        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // Process data received from the background thread
                try {

                    if (msg.what == COMMUNITY_CARDS_MSG) {
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
                            // Delay for a specified time before proceeding to reset the game state
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Code to reset the game state goes here
                                    cleanUp();
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
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        cardFragment = (CardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_card);

    }

    private void cleanUp() {
        renderImagesTemp();
        resetPlayerPositions();
        communityCardIds.clear();
        pot = 0;
        remainPlayers = null;
        winner = null;
        dataObject = null;
    }

    private void resetPlayerPositions() {
        for(RelativeLayout layout : playerPositions)
            layout.setVisibility(View.INVISIBLE);
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

    private void revealHands(){
//        this.remainPlayers = this.players.stream().filter(player -> !player.hasFolded()).collect(Collectors.toList());
        for(int i = 0; i < remainPlayers.size(); i++) {
            GridView view = playerViews.get(players.indexOf(remainPlayers.get(i)));
            List<Integer> playerCardIds = remainPlayers.get(i).getHand()
                                        .stream().map(card -> CardUtils.getCardImageResourceId(card.toString(), getApplicationContext()))
                                        .collect(Collectors.toList());

            view.setAdapter(new CardAdapter(this, playerCardIds));
        }
    }

    @Override
    public void onPlayerTurnUpdate(){
        PlayerCountdownView playerCountdownView;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                String resourceName = "player"+ currentPlayerIndex + "Countdown";
                Context context = getApplicationContext();
                int resourceId = context.getResources().getIdentifier(resourceName, "id", context.getPackageName());

                PlayerCountdownView playerCountdownView = findViewById(resourceId);
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
    protected void onDestroy(){
        super.onDestroy();
        try {
            controllerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
    }
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
        for(int i = 0; i < 5; i++){
            players.add(new BotPlayer(
                    "Player "+ i, 50000, handler, getApplicationContext()));
        }

        // Listening to GameController if anything update.
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            //Background work here
            handleMessage();
        });

        // Instantiate GameController
        this.gameController = new GameController(players,
                handler,
                getApplicationContext(),
                playerThreadPool,
                objectLocker);

        // Set Controller to every player and launch Thread
        for(Player player : players) {
            player.setController(gameController);
            playerThreadPool.submit((Runnable) player);
        }

        // Instantiate controllerThread and start it.
        this.controllerThread = new Thread(gameController);
        controllerThread.start();

    }

    private void setupPlayerPositions() {
        playerPositions.add(findViewById(R.id.posIconPlayer0Layout));
        playerPositions.add(findViewById(R.id.posIconPlayer1Layout));
        playerPositions.add(findViewById(R.id.posIconPlayer2Layout));
        playerPositions.add(findViewById(R.id.posIconPlayer3Layout));
        playerPositions.add(findViewById(R.id.posIconPlayer4Layout));
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
    public void onClickExitBtn(View view){
        Intent intent = new Intent(HostActivity.this, MainActivity.class);
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
        Intent intent = new Intent("com.example.flush_poker_android.ACTION");
        intent.putExtra("action", action);

        // Start the service or broadcast the intent
        sendBroadcast(intent);
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
}