package com.abhi.wifidirect;

import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.view.View;

public class ConnectionListener implements ConnectionInfoListener{
static WifiP2pInfo peerinfo;
View cView;
	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo arg0) {
		// TODO Auto-generated method stub
		peerinfo=arg0;
		//View v=new View()
	}

}
