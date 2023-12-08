package com.example.flush_poker_android.network;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * The DisconnectTask class is responsible for disconnecting from a WiFi Direct (P2P) group.
 * It performs the disconnection process asynchronously.
 *
 * @author Kyle Chainey
 */
public class DisconnectTask extends AsyncTask<Void, Void, Boolean> {
    private WifiP2pManager manager = null;
    private WifiP2pManager.Channel channel = null;
    private Handler uiHandler = null;
    private final String TAG = "DisconnectTask";
    private final Context context;

    /**
     * Constructs a DisconnectTask with the required parameters.
     *
     * @param manager  The WiFiP2pManager instance.
     * @param channel  The WiFiP2pManager.Channel instance.
     * @param context  The application context.
     * @param handler  The UI handler for displaying toasts.
     */
    public DisconnectTask(WifiP2pManager manager, WifiP2pManager.Channel channel, Context context, Handler handler) {
        this.manager = manager;
        this.channel = channel;
        this.context = context;
        uiHandler = handler;
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
    /**Overridden AsyncTask method that handles disconnects in the background*/
    protected Boolean doInBackground(Void... voids) {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason: " + reasonCode);
            }

            @Override
            public void onSuccess() {
                updateToast("Disconnect Successful");
            }
        });
        return true;
    }
}
