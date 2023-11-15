package com.example.flush_poker_android.network;

import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.text.PrecomputedText;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.flush_poker_android.Client.DeviceListFragment;
import com.example.flush_poker_android.Client.P2PActivity;

public class PeerDiscoveryTask extends AsyncTask<Void, Void, Boolean> {
    private WifiP2pManager manager = null;
    private WifiP2pManager.Channel channel;
    private P2PActivity activity = null;
    private DeviceListFragment fragment = null;

    public PeerDiscoveryTask(WifiP2pManager manager, WifiP2pManager.Channel channel, P2PActivity activity, DeviceListFragment fragment){
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        this.fragment = fragment;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(activity, "Discovery Initiated",
                        Toast.LENGTH_SHORT).show();
                Log.i("Init Peer Discover", fragment.getDevice().toString());
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(activity, "Discovery Failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
            }
        });
        return true;
    }
}
