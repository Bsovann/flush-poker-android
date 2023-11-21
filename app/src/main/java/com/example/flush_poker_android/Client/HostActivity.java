package com.example.flush_poker_android.Client;

import android.app.Dialog;
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
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flush_poker_android.Client.Utility.GameInfo;
import com.example.flush_poker_android.Client.customviews.CardAdapter;
import com.example.flush_poker_android.Logic.BotPlayer;
import com.example.flush_poker_android.Logic.GameController;
import com.example.flush_poker_android.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class HostActivity extends AppCompatActivity implements GameUpdateListener {
    private Dialog dialog;
    private SeekBar brightnessSeekBar;
    private float screenBrightness = 127 / 255.0f;
    GridView communityCardView;
    List<GridView> playersView;
    List<CardAdapter> playerAdapter;
    CardAdapter commnityCardAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<BotPlayer> players;
    private int pot;
    private Thread controllerThread;
    private ExecutorService playerThreadPool;
    private GameController gameController;
    private CardFragment cardFragment;
    private Set<Integer> communityCardIds = new HashSet<>();

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
                if (msg.what == 1) {
                    Bundle bundle = msg.getData();
                    GameInfo dataObject = (GameInfo) bundle.getSerializable("dataObject");
                    if (dataObject != null) {
                        // Check if the message is new based on its timestamp
                        // Process the new data and call onResume
                        communityCardIds.addAll(dataObject.getCommunityCardIds());
                        onResume();

                        Log.i("HandleMessage", communityCardIds.toString());
                    }
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        cardFragment = (CardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_card);
//      handleMessage();
        renderNewUI();
    }

    private void renderNewUI(){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                onCardUpdate(communityCardIds.stream().collect(Collectors.toList()));
                // update playerTurn position
                // update current pot
                // update dealer position
                // update the winner
            }
        });
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
        playersView = new ArrayList<>(5);
        playerAdapter = new ArrayList<>(5);
        players = new ArrayList<>();

        // Default info rendering
        renderImagesTemp();

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
        this.gameController = new GameController(players, handler, getApplicationContext(), playerThreadPool);

        // Set Controller to every player and launch Thread
        for(BotPlayer player : players) {
            player.setController(gameController);
            playerThreadPool.submit(player);
        }

        // Instantiate controllerThread and start it.
        this.controllerThread = new Thread(gameController);
        controllerThread.start();

    }
    private void renderImagesTemp(){
        playersView.add(findViewById(R.id.myCards));
        playersView.add(findViewById(R.id.player1Cards));
        playersView.add(findViewById(R.id.player2Cards));
        playersView.add(findViewById(R.id.player3Cards));
        playersView.add(findViewById(R.id.player4Cards));

        // Add players cards images and render
        for(int i = 0; i < 5; i++) {
            playerAdapter.add(new CardAdapter(this, Arrays.asList(R.drawable.back_of_card1, R.drawable.back_of_card1)));
        }

        // Render card
        for(int i = 0; i < 5; i++) {
            playersView.get(i).setAdapter(playerAdapter.get(i));
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
    public void onCardUpdate(List<Integer> updatedCardImages) {
        if (cardFragment != null) {
            cardFragment.onCardUpdate(updatedCardImages);
        } else {
            // Handle the case where cardFragment is null
            Log.e("YourActivity", "cardFragment is null");
        }
    }
}