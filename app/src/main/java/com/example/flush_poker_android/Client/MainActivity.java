package com.example.flush_poker_android.Client;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.example.flush_poker_android.R;
import com.example.flush_poker_android.customviews.ChallengeButton;

public class MainActivity extends AppCompatActivity {

    private Dialog dialog;
    private Button closeSettingBtn;
    private SeekBar brightnessSeekBar;
    private float screenBrightness = 127 / 255.0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new Dialog(this);

        // Enable immersive mode
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Set screen orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_main);
    }

    public void onClickFindMatchBtn(View view){
        Button findMatchBtn = findViewById(R.id.findMatchButton);
        Log.i("Button", "Find Match got clicked");
    }
    public void onClickChallengeBtn(View view){
        ChallengeButton challengeBtn = findViewById(R.id.challengeButton);
        Log.i("Button", "Challenge Button got clicked");

    }

    public void onClickSettingIcon(View view){

        // SettingDialog
        dialog.setContentView(R.layout.setting_dialog);
        brightnessSeekBar = dialog.findViewById(R.id.brightnessSeekBar);
        closeSettingBtn = dialog.findViewById(R.id.closeSettingButton);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        brightnessSeekBar.setProgress((int) (screenBrightness * 255));
        dialog.show();

        // Brightness control
        setupSeekBarListener();

        // Exit button
        closeSettingBtn = dialog.findViewById(R.id.closeSettingButton);
        closeSettingBtn.setOnClickListener(x -> dialog.dismiss());
    }

    public void onClickInstructionIcon(View view){
        RelativeLayout instructionIcon = findViewById(R.id.instructionIcon);
        Log.i("Icon", "Instruction Icon got clicked");
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