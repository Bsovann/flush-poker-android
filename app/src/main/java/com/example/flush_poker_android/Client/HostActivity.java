package com.example.flush_poker_android.Client;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flush_poker_android.Client.customviews.CardAdapter;
import com.example.flush_poker_android.Logic.BotPlayer;
import com.example.flush_poker_android.Logic.GameController;
import com.example.flush_poker_android.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HostActivity extends AppCompatActivity {
    private Dialog dialog;
    private SeekBar brightnessSeekBar;
    private float screenBrightness = 127 / 255.0f;
    GridView communityCardView;
    List<GridView> playersView;
    List<CardAdapter> playerAdapter;
    CardAdapter commnityCardAdapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private List<BotPlayer> players;
    private int pot;
    private Thread controllerThread;
    private ExecutorService playerThreadPool;
    private GameController gameController;
    private CardFragment cardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initWork();

        // Create CardFragment instance
        cardFragment = new CardFragment();

        // Check if the fragment is already added
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_card, cardFragment) // R.id.fragment_container is the ID of a FrameLayout in your activity's layout
                    .commit();
        }




    }

    @Override
    public void onResume() {
        super.onResume();
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

        dialog = new Dialog(this);

        playersView = new ArrayList<>(5);
        communityCardView = findViewById(R.id.communityCardGrid);
        playerAdapter = new ArrayList<>(5);
        players = new ArrayList<>();

        renderImagesTemp();
        this.playerThreadPool = Executors.newFixedThreadPool(5);
        // Assign Each player to each Thread
        for(int i = 0; i < 5; i++){
            players.add(new BotPlayer(
                    "Player "+ i, 50000, handler, getApplicationContext()));
        }

        this.gameController = new GameController(players, handler,getApplicationContext(), playerThreadPool);

        // Add CardFragment as a listener to the GameController
        gameController.addGameUpdateListener(cardFragment);


        // Set Controller to every player and launch Thread
        for(BotPlayer player : players) {
            player.setController(gameController);
            playerThreadPool.submit(player);
        }
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
        List<Integer> communityCardImages = new LinkedList<>();
        // Add Community cards images
        communityCardImages.add(R.drawable.back_of_card);
        communityCardImages.add(R.drawable.back_of_card);
        communityCardImages.add(R.drawable.back_of_card);
        communityCardImages.add(R.drawable.back_of_card);
        communityCardImages.add(R.drawable.back_of_card);
        Collections.reverse(communityCardImages);
        commnityCardAdapter = new CardAdapter(this, communityCardImages);
        communityCardView.setAdapter(commnityCardAdapter);
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
}