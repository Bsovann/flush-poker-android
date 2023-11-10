package com.example.flush_poker_android.Client;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.flush_poker_android.Client.customviews.CardAdapter;
import com.example.flush_poker_android.Logic.BettingLogic;
import com.example.flush_poker_android.Logic.TexasHoldEmGamePracticeMode;
import com.example.flush_poker_android.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    private Dialog dialog;
    private SeekBar brightnessSeekBar;
    private float screenBrightness = 127 / 255.0f;
    GridView communityCardView;
    List<GridView> playersView;

    List<CardAdapter> playerAdapter;
    CardAdapter commnityCardAdapter;

    private TextView timerTextView;
    private BettingLogic bettingLogic;
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private TexasHoldEmGamePracticeMode gameLogic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initWork();

        gameLogic.startPlayerTurn();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.isRunning = false;
        gameLoop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        gameLogic.unregisterReceiver(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        isRunning = false;
    }

    private void gameLoop() {
        if (isRunning) {
            startTimer(30000); // 30 seconds
            // Handle user input (e.g., touch events) to make player moves
            //pokerGameView.handleInput();
//
//            // Update the game state
//            pokerGameLogic.update();
//
//            // Render the game view
//            pokerGameView.invalidate(); // This will trigger a redraw of the game view
            Toast.makeText(this, "Test", Toast.LENGTH_SHORT).show();

            // Define your desired frame rate (e.g., 30 FPS)
            int frameRate = 1000 / 30;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    gameLoop(); // Call the game loop again after a delay
                }
            }, frameRate);
        }
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

        //
        timerTextView = findViewById(R.id.timerTextView); // Add a TextView for the timer
        // Start the countdown timer with a 30-second limit (adjust as needed)

        gameLogic = new TexasHoldEmGamePracticeMode(this, handler);


    }

    public void onClickExitBtn(View view){
        Intent intent = new Intent(GameActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void onClickFoldBtn(View view){
        Toast.makeText(this, "Fold Btn got Clicked!", Toast.LENGTH_SHORT).show();
        handlePlayerAction("Fold");
    }

    public void onClickCheckBtn(View view){
        Toast.makeText(this, "Check Btn got Clicked!", Toast.LENGTH_SHORT).show();
        handlePlayerAction("Check");
    }

    public void onClickCallBtn(View view){
        Toast.makeText(this, "Call Btn got Clicked!", Toast.LENGTH_SHORT).show();
        handlePlayerAction("Call");
    }

    public void onClickBetBtn(View view){
        Toast.makeText(this, "Bet Btn got Clicked!", Toast.LENGTH_SHORT).show();
        handlePlayerAction("Bet");
    }

    private void handlePlayerAction(String action) {
        Intent intent = new Intent("com.example.flush_poker_android.ACTION");
        intent.putExtra("action", action);

        // Start the service or broadcast the intent
        sendBroadcast(intent);
    }

    private void startTimer(long milliseconds) {
        countDownTimer = new CountDownTimer(milliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setText("Time remaining: " + millisUntilFinished / 1000 + " seconds");
            }
            @Override
            public void onFinish() {
                // Perform actions when the timer finishes (e.g., force the player to fold)
                handlePlayerAction("Fold");
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            timerTextView.setText("Timer stopped");
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
}