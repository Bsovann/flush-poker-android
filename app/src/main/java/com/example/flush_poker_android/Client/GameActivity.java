package com.example.flush_poker_android.Client;
import androidx.appcompat.app.AppCompatActivity;
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

import com.example.flush_poker_android.Client.customviews.CardAdapter;
import com.example.flush_poker_android.Logic.Player;
import com.example.flush_poker_android.Logic.GameController;
import com.example.flush_poker_android.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameActivity extends AppCompatActivity {
    private Dialog dialog;
    private SeekBar brightnessSeekBar;
    private float screenBrightness = 127 / 255.0f;
    GridView communityCardView;
    List<GridView> playersView;
    List<CardAdapter> playerAdapter;
    CardAdapter commnityCardAdapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private List<Player> players;
    private int pot;
    private Thread controllerThread;
    ExecutorService playerThreadPool;
    private GameController gameController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initWork();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
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

        this.gameController = new GameController(handler,getApplicationContext());

        // Assign Each player to each Thread
        for(int i = 0; i < 5; i++){
            players.add(new Player("Player "+ i, 50000, gameController, handler, getApplicationContext()));
        }

        // Start Controller
        this.gameController.setPlayersAndInitGame(players);
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
        communityCardImages.add(R.drawable.ace_of_diamonds);
        communityCardImages.add(R.drawable.ace_of_hearts);
        communityCardImages.add(R.drawable.jack_of_clubs);
        communityCardImages.add(R.drawable.nine_of_clubs);
        communityCardImages.add(R.drawable.eight_of_clubs);
        Collections.reverse(communityCardImages);
        commnityCardAdapter = new CardAdapter(this, communityCardImages);
        communityCardView.setAdapter(commnityCardAdapter);
    }
    public void onClickExitBtn(View view){
        Intent intent = new Intent(GameActivity.this, MainActivity.class);
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