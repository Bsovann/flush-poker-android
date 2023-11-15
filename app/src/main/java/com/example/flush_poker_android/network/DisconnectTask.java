package com.example.flush_poker_android.network;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import android.content.Context;

import com.example.flush_poker_android.Client.MainActivity;

public class DisconnectTask extends AsyncTask<Void, Void, Boolean> {
    private WifiP2pManager manager = null;
    private WifiP2pManager.Channel channel = null;

    private final String TAG = "DisconnectTask";
    private Context context;
    public DisconnectTask(WifiP2pManager manager, WifiP2pManager.Channel channel, Context context){
        this.manager = manager;
        this.channel = channel;
        this.context = context;
    }
    @Override
    protected Boolean doInBackground(Void... voids) {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Peer Connected", Toast.LENGTH_SHORT).show();
            }
        });
    }
}