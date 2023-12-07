package com.example.flush_poker_android.network;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;
import android.content.Context;

import com.example.flush_poker_android.Client.MainActivity;

public class ConnectTask extends AsyncTask<Void, Void, Boolean> {
    private WifiP2pManager manager = null;
    private WifiP2pConfig config = null;
    private WifiP2pManager.Channel channel = null;
    private Handler uiHandler;
    private Context context = null;
    public ConnectTask(Context context, WifiP2pManager manager, WifiP2pManager.Channel channel, WifiP2pConfig config, Handler uiHandler){
        this.manager = manager;
        this.config = config;
        this.channel = channel;
        this.uiHandler = uiHandler;
        this.context = context;
    }

    private void updateToast(String s){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, s,
                        Toast.LENGTH_SHORT).show();
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