package com.example.flush_poker_android.Client;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import com.example.flush_poker_android.R;

public class GameActivity extends AppCompatActivity {
    private Dialog dialog;
    private SeekBar brightnessSeekBar;
    private float screenBrightness = 127 / 255.0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        dialog = new Dialog(this);

        // Enable immersive mode
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Set screen orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_game);
    }

    public void onClickExitBtn(View view){
        Intent intent = new Intent(GameActivity.this, MainActivity.class);
        startActivity(intent);
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