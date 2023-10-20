package com.example.flush_poker_android.Client;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.flush_poker_android.R;
import com.example.flush_poker_android.customviews.ChallengeButton;

public class MainActivity extends AppCompatActivity {
    private int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        Dialog settingDialog = new Dialog(this);
        settingDialog.setContentView(R.layout.setting_dialog);
        settingDialog.show();

        // Exit button
        Button closeSettingBtn = settingDialog.findViewById(R.id.closeSettingButton);
        closeSettingBtn.setOnClickListener(x -> settingDialog.dismiss());

    }

    public void onClickInstructionIcon(View view){
        RelativeLayout instructionIcon = findViewById(R.id.instructionIcon);
        Log.i("Icon", "Instruction Icon got clicked");
    }
}