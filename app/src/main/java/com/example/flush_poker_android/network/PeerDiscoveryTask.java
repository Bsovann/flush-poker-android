package com.example.flush_poker_android.network;

import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.flush_poker_android.Client.MainActivity;

/**
 * The PeerDiscoveryTask class is responsible for initiating peer discovery in a WiFi Direct (P2P) network.
 * It performs peer discovery asynchronously.
 *
 * @author Kyle Chainey
 */
public class PeerDiscoveryTask extends AsyncTask<Void, Void, Boolean> {
    private WifiP2pManager manager = null;
    private WifiP2pManager.Channel channel;
    private MainActivity activity = null;

    /**
     * Constructs a PeerDiscoveryTask with the required parameters.
     *
     * @param manager  The WifiP2pManager instance.
     * @param channel  The WifiP2pManager.Channel instance.
     * @param activity The MainActivity instance.
     */
    public PeerDiscoveryTask(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(activity, "Discovery Initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(activity, "Discovery Failed : " + reasonCode, Toast.LENGTH_SHORT).show();
            }
        });
        return true;
    }
}
