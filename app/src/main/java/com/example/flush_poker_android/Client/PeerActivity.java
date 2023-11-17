package com.example.flush_poker_android.Client;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;

import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flush_poker_android.Client.customviews.CardAdapter;
import com.example.flush_poker_android.Logic.GameController;
import com.example.flush_poker_android.Logic.Player;
import com.example.flush_poker_android.R;
import com.example.flush_poker_android.network.DisconnectTask;
import com.example.flush_poker_android.network.PeerDiscoveryTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**Activity for connecting a player to a host's game*/
public class PeerActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener, DeviceListFragment.DeviceActionListener{
    private static final String TAG = "";
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pDevice device; // this peer's device info
    private boolean retryChannel = false;
    private BroadcastReceiver receiver = null;
    private Dialog dialog;
    private SeekBar brightnessSeekBar;
    private float screenBrightness = 127 / 255.0f;
    GridView communityCardView;
    List<GridView> playersView;
    List<CardAdapter> playerAdapter;
    CardAdapter commnityCardAdapter;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private List<Player> players;
    private int pot;
    private Thread controllerThread;
    ExecutorService playerThreadPool;
    private GameController gameController;
    private Boolean isWifiP2pEnabled = false;

    private WifiP2pDevice dev = null;
    private WifiP2pConfig config = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initWork();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            // Retrieve the WifiP2pDevice object
            dev = bundle.getParcelable("device");

            // Retrieve the WifiP2pConfig object
            config = bundle.getParcelable("config");

        }
        Log.i("ConnectAction", "Connected to: " + dev.deviceName);
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

        this.playerThreadPool = Executors.newFixedThreadPool(5);

        // Assign Each player to each Thread
        for(int i = 0; i < 5; i++){
            players.add(new Player(
                    "Player "+ i, 50000, uiHandler, getApplicationContext()));
        }

        this.gameController = new GameController(players, uiHandler,getApplicationContext());

        // Set Controller to every player and launch Thread
        for(Player player : players) {
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
        for(int i = 0; i < 5; i++)
            playerAdapter.add(new CardAdapter(this, Arrays.asList(R.drawable.back_of_card1, R.drawable.back_of_card1)));

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
        //TODO: create method that unassigns player from gameController upon exit
        disconnect();
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
                //do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //do nothing
            }
        });
    }
    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);
    }
    @Override
    public void cancelDisconnect() {
        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {
                manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(PeerActivity.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(PeerActivity.this,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
    @Override
    public void connect(WifiP2pConfig config) {
        com.example.flush_poker_android.network.ConnectTask connect = new com.example.flush_poker_android.network.ConnectTask(PeerActivity.this ,manager, channel, config, uiHandler);

        connect.execute();
    }
    @Override
    public void disconnect() {
        DisconnectTask disconnect = new DisconnectTask(manager, channel, PeerActivity.this, uiHandler);
        disconnect.execute();
    }
    @Override
    public void onChannelDisconnected() {
        // try to reconnect
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost permanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Remove all peers and clear all fields. This is called when disconnecting a channel.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
    }

}