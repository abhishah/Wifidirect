package com.abhi.wifidirect;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.widget.Toast;

public class BroadcastManager extends BroadcastReceiver {
	private WifiP2pManager wifimanager;
	private Channel mchannel;
	private Main myactivity;
	PeerListListener myPeerListListener;

	public BroadcastManager(WifiP2pManager a, Channel b, Main activity) {
		this.wifimanager = a;
		this.mchannel = b;
		this.myactivity = activity;
		myPeerListListener = activity;
		Toast.makeText(activity, "Broadcast initiated", Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public void onReceive(Context arg0, Intent intent) {
		
		String action = intent.getAction();
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				Toast.makeText(arg0, "WifiP2p enabled", Toast.LENGTH_SHORT)
						.show();
			} else {
				// Wi-Fi P2P is not enabled
				Toast.makeText(arg0, "WifiP2p not enabled", Toast.LENGTH_SHORT)
						.show();
			}

		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			Toast.makeText(myactivity, "Broadcast request peers",
					Toast.LENGTH_SHORT).show();
			if (wifimanager != null) {
				wifimanager.requestPeers(mchannel, myPeerListListener);
				Toast.makeText(myactivity, "Broadcast request peers",
						Toast.LENGTH_SHORT).show();
			}

		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
				.equals(action)) {
			if(wifimanager ==null){return;}
			NetworkInfo info=(NetworkInfo)intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			if(info.isConnected()){
				wifimanager.requestConnectionInfo(mchannel, new ConnectionListener());
			}

		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
				.equals(action)) {

		}
	}

}
