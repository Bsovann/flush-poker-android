package com.example.flush_poker_android.Client;

import static com.example.flush_poker_android.Client.MainActivity.TAG;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
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
import androidx.core.app.ActivityCompat;

import com.example.flush_poker_android.Client.customviews.CardAdapter;
import com.example.flush_poker_android.Client.customviews.PlayerCountdownView;
import com.example.flush_poker_android.Logic.BotPlayer;
import com.example.flush_poker_android.Logic.GameController;
import com.example.flush_poker_android.Logic.Player;
import com.example.flush_poker_android.Logic.Utility.CardUtils;
import com.example.flush_poker_android.Logic.Utility.GameInfo;
import com.example.flush_poker_android.R;
import com.example.flush_poker_android.network.ServerBroadcastReceiver;

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
    List<GridView> playerViews;
    List<CardAdapter> playerAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<Player> players;
    private int pot;
    private Thread controllerThread;
    private ExecutorService playerThreadPool;
    private GameController gameController;
    private CardFragment cardFragment;
    private final List<Integer> communityCardIds = new ArrayList<>();
    private List<Player> remainPlayers;
    private int currentPlayerIndex;
    private int dealerPosition;
    private Player winner;
    private final Semaphore remainingSeats = new Semaphore(4); // TODO: allow player to set this. Max total seats = 5
    private final List<RelativeLayout> playerPositions = new ArrayList<>();
    GameInfo dataObject = null;
    private static final int COMMUNITY_CARDS_MSG = 1;
    private static final int REMAIN_PLAYERS_MSG = 2;
    private static final int DEALER_INDEX_MSG = 3;
    private static final int WINNER_MSG = 4;
    private static final int POT_MSG = 5;
    private static final int PLAYER_INDEX_MSG = 6;
    private static final int CURRENT_PLAYER_ACTION_MSG = 7;
    // Wifi Direct
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1001;
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private final boolean retryChannel = false;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private ServerBroadcastReceiver receiver = null;

    /////////////////////////// End Wifi Direct Instances //////////////////////
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
        initP2p();
        initDiscover();
    }

    private boolean initP2p() {
        // Device capability definition check
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Log.e(TAG, "Wi-Fi Direct is not supported by this device.");
            return false;
        }
        // Hardware capability check
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager == null) {
            Log.e(TAG, "Cannot get Wi-Fi system service.");
            return false;
        }else
            wifiManager.setWifiEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!wifiManager.isP2pSupported()) {
                Log.e(TAG, "Wi-Fi Direct is not supported by the hardware or Wi-Fi is off.");
                return false;
            }
        }
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        if (manager == null) {
            Log.e(TAG, "Cannot get Wi-Fi Direct system service.");
            return false;
        }
        channel = manager.initialize(this, getMainLooper(), null);
        if (channel == null) {
            Log.e(TAG, "Cannot initialize Wi-Fi Direct.");
            return false;
        }
        return true;
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
                    } else if (msg.what == CURRENT_PLAYER_ACTION_MSG) {
//                        Bundle bundle = msg.getData();
                        onPlayerActionUpdate();
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        };
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

    @Override
    public void onResume() {
        super.onResume();
        cardFragment = (CardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_card);
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(() -> {
            receiver = new ServerBroadcastReceiver(manager, channel, this);
        });
    }

    private void cleanUp() {
        renderImagesTemp();
        resetPlayerPositions();
        renderPlayerInfo();
        communityCardIds.clear();
        pot = 0;
        remainPlayers = null;
        winner = null;
        dataObject = null;
    }

    private void renderPlayerInfo() {
        for (int i = 0; i < players.size(); i++) {
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
        for (RelativeLayout layout : playerPositions)
            layout.setVisibility(View.INVISIBLE);
    }

    private void onUpdatePlayerPosition() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 3; i++) {
                    int posIndex = (dealerPosition + i) % playerPositions.size();
                    RelativeLayout layout = playerPositions.get(posIndex);
                    TextView textView = (TextView) layout.getChildAt(0);

                    if (posIndex == dealerPosition)
                        textView.setText("D");
                    else if (posIndex == dealerPosition + 1)
                        textView.setText("SB");
                    else
                        textView.setText("BB");

                    layout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void revealHands() {
//        this.remainPlayers = this.players.stream().filter(player -> !player.hasFolded()).collect(Collectors.toList());
        for (int i = 0; i < remainPlayers.size(); i++) {
            GridView view = playerViews.get(players.indexOf(remainPlayers.get(i)));
            List<Integer> playerCardIds = remainPlayers.get(i).getHand()
                    .stream().map(card -> CardUtils.getCardImageResourceId(card.toString(), getApplicationContext()))
                    .collect(Collectors.toList());

            view.setAdapter(new CardAdapter(this, playerCardIds));
        }
    }

    @Override
    public void onPlayerTurnUpdate() {
        PlayerCountdownView playerCountdownView;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                String resourceName = "player" + currentPlayerIndex + "Countdown";
                Context context = getApplicationContext();
                int resourceId = context.getResources().getIdentifier(resourceName, "id", context.getPackageName());

                PlayerCountdownView playerCountdownView = findViewById(resourceId);
                playerCountdownView.setVisibility(View.VISIBLE);

                playerCountdownView.startCountdown(10000); // Start a 30-second countdown
            }
        });
    }

    @Override
    public void onPotUpdate() {
        TextView pot = findViewById(R.id.pot);
        pot.setText(String.format("Pot: %d$", this.pot));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            controllerThread.join();
            playerThreadPool.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initWork() {

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

        // add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    HostActivity.PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
            // After this point you wait for callback in
            // onRequestPermissionsResult(int, String[], int[]) overridden method
        }

        // Default info rendering
        renderImagesTemp();
        setupPlayerPositions();


        // Listening to GameController if anything update.
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            //Background work here
            handleMessage();
        });

        // Instantiate GameController
        this.gameController = new GameController(handler, getApplicationContext(), playerThreadPool, remainingSeats);

        // Render player info.
        renderPlayerInfo();

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

    private void renderImagesTemp() {
        playerViews.add(findViewById(R.id.myCards));
        playerViews.add(findViewById(R.id.player1Cards));
        playerViews.add(findViewById(R.id.player2Cards));
        playerViews.add(findViewById(R.id.player3Cards));
        playerViews.add(findViewById(R.id.player4Cards));

        // Add players cards images and render
        for (int i = 0; i < 5; i++) {
            playerAdapter.add(new CardAdapter(this, Arrays.asList(R.drawable.back_of_card1, R.drawable.back_of_card1)));
        }

        // Render card
        for (int i = 0; i < 5; i++) {
            playerViews.get(i).setAdapter(playerAdapter.get(i));
        }
    }

    public void onClickExitBtn(View view) {
        Intent intent = new Intent(HostActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void onClickFoldBtn(View view) {
        handlePlayerAction("Fold");
    }

    public void onClickCheckBtn(View view) {
        handlePlayerAction("Check");
    }

    public void onClickCallBtn(View view) {
        handlePlayerAction("Call");
    }

    public void onClickBetBtn(View view) {
        handlePlayerAction("Bet");
    }

    private void handlePlayerAction(String action) {
        Intent intent = new Intent("com.example.flush_poker_android.ACTION");
        intent.putExtra("action", action);

        // Start the service or broadcast the intent
        sendBroadcast(intent);
    }

    public void onClickChatIcon(View view) {
        // Chat Dialog
        dialog.setContentView(R.layout.chat_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        // Exit button
        Button sendChatBtn = dialog.findViewById(R.id.sendChatButton);
        sendChatBtn.setOnClickListener(x -> dialog.dismiss());
    }

    public void onClickSettingIcon(View view) {

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

    public void onClickInstructionIcon(View view) {
        // Instruction Dialog
        dialog.setContentView(R.layout.instruction_dialog);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        // Exit button
        Button closeInstructionBtn = dialog.findViewById(R.id.closeInstructionButton);
        closeInstructionBtn.setOnClickListener(x -> dialog.dismiss());
    }

    public void setupSeekBarListener() {
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int currentValue, boolean fromUser) {
                if (fromUser) {
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

    public void initDiscover() {
        if (!isWifiP2pEnabled) {
//            Toast.makeText(HostActivity.this, R.string.p2p_off_warning,
//                    Toast.LENGTH_SHORT).show();
        }

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i("Networking - Server Side", "Init Peer Discover");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.i("Networking - Server Side", "Peer Discovery Failed. Reason code: " + reasonCode);
            }
        });
    }

    public void setIsWifiP2pEnabled(boolean b) {
        isWifiP2pEnabled = b;
    }
}