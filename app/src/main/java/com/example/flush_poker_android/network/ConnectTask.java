package com.example.flush_poker_android.network;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

/**
 * The ConnectTask class is responsible for establishing a connection between devices
 * in a WiFi Direct (P2P) network.
 *
 * @author Kyle Chainey
 */
public class ConnectTask extends AsyncTask<Void, Void, Boolean> {
    private WifiP2pManager manager = null;
    private WifiP2pConfig config = null;
    private WifiP2pManager.Channel channel = null;
    private Handler uiHandler;
    private Context context = null;

    /**
     * Constructs a ConnectTask with the required parameters.
     *
     * @param context   The application context.
     * @param manager   The WiFiP2pManager instance.
     * @param channel   The WiFiP2pManager.Channel instance.
     * @param config    The WiFiP2pConfig for the connection.
     * @param uiHandler The UI handler for displaying toasts.
     */
    public ConnectTask(Context context, WifiP2pManager manager, WifiP2pManager.Channel channel, WifiP2pConfig config, Handler uiHandler) {
        this.manager = manager;
        this.config = config;
        this.channel = channel;
        this.uiHandler = uiHandler;
        this.context = context;
    }

    /**
     * Displays a toast message on the UI thread.
     *
     * @param message The message to be displayed.
     */
    private void updateToast(String message) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                updateToast("Connect failed. Retry.");
            }
        });
        return true;
    }
}
