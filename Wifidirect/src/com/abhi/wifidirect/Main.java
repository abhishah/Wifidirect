package com.abhi.wifidirect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("unused")
public class Main extends Activity implements WifiP2pManager.PeerListListener,
		OnClickListener, OnItemClickListener {
	WifiP2pManager wifimanager;
	Channel channel;
	BroadcastManager Bmanager;
	IntentFilter mIntentFilter;
	WifiP2pDevice device;
	static WifiP2pConfig config;
	static EditText message;
	TextView status;
	Button send, scan;
	ListView list;
	WifiManager wifi;
	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setView();
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled() == false) {
			wifi.setWifiEnabled(true);
		}
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		wifimanager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = wifimanager.initialize(this, getMainLooper(), null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setView() {
		message = (EditText) findViewById(R.id.etmessage);
		send = (Button) findViewById(R.id.bdata);
		send.setVisibility(Button.INVISIBLE);
		scan = (Button) findViewById(R.id.bpeers);
		list = (ListView) findViewById(R.id.peers);
		status = (TextView) findViewById(R.id.tvStatus);
		list.setAdapter(new ArrayAdapter(this,
				android.R.layout.simple_list_item_1, peers));
		send.setOnClickListener(this);
		scan.setOnClickListener(this);
		list.setOnItemClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(Bmanager);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Bmanager = new BroadcastManager(wifimanager, channel, this);
		registerReceiver(Bmanager, mIntentFilter);
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList arg0) {
		Toast.makeText(getBaseContext(), "onPeersAvailable", Toast.LENGTH_SHORT)
				.show();
		// TODO Auto-generated method stub
		Collection<WifiP2pDevice> a = arg0.getDeviceList();
		peers.clear();
		peers.addAll(a);
	}

	public void scan() {
		Toast.makeText(getBaseContext(), "scan success", Toast.LENGTH_SHORT)
				.show();
		wifimanager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				Toast.makeText(getBaseContext(), "discovery success",
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				Toast.makeText(getBaseContext(), "discovery Failed" + reason,
						Toast.LENGTH_SHORT).show();
			}
		});
		// config=new WifiP2pConfig();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.bpeers:
			scan();
			break;
		case R.id.bdata:
			if (send.getVisibility() == Button.VISIBLE) {
				if (ConnectionListener.peerinfo.isGroupOwner)
					new FileServerAsynTask(this.getApplicationContext(),
							new View(this)).execute();
				else {
					new FileClientAsynTask(this.getApplicationContext(),
							new View(this)).execute();
				}
			}
			break;
		}
	}

	public void connect() {
		config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		wifimanager.connect(channel, config, new ActionListener() {
			public void onSuccess() {
				Toast.makeText(getBaseContext(), "connection success",
						Toast.LENGTH_SHORT).show();
				send.setVisibility(Button.VISIBLE);
			}

			public void onFailure(int reason) {
				Toast.makeText(getBaseContext(), "connection failure" + reason,
						Toast.LENGTH_SHORT).show();
				send.setVisibility(Button.INVISIBLE);
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		device = (WifiP2pDevice) arg0.getItemAtPosition(arg2);
		connect();
	}

	@SuppressWarnings("rawtypes")
	public static class FileServerAsynTask extends AsyncTask {
		private Context context;
		private TextView statustext;

		public FileServerAsynTask(Context context, View statusText) {
			this.context = context;
			this.statustext = (TextView) statusText;
		}

		protected Object doInBackground(Object... arg0) {
			try { // TODO Auto-generated method stub
				ServerSocket serverSocket = new ServerSocket(8997);
				Socket client = serverSocket.accept();
				final File f = new File(
						Environment.getExternalStorageDirectory() + "/"
								+ context.getPackageName() + "/wifip2pshared-"
								+ System.currentTimeMillis() + ".txt");
				File dirs = new File(f.getParent());
				if (!dirs.exists())
					dirs.mkdirs();
				f.createNewFile();
				InputStream inputstream = client.getInputStream();
				copyFile(inputstream, new FileOutputStream(f));
				serverSocket.close();
				return f.getAbsolutePath();

			} catch (IOException e) {
				Log.e("error", e.getMessage());
				return null;
			}
		}

		public void copyFile(InputStream i, FileOutputStream f) {
			try {
				int byteread;
				byte b[] = new byte[4096];

				while ((byteread = i.read(b)) != -1) {
					f.write(b, 0, byteread);
				}
				i.close();
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static class FileClientAsynTask extends AsyncTask {
		Context context;
		String host;
		int port;
		int len;
		View info;
		Socket socket = new Socket();
		byte buf[] = new byte[1024];

		public FileClientAsynTask(Context xontext, View status) {
			this.context = xontext;
			this.info = status;
		}

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			try {
				socket.bind(null);
				socket.connect((new InetSocketAddress(8997)), 500);
				OutputStream out = socket.getOutputStream();
				ContentResolver cr = context.getContentResolver();
				InputStream in = null;
				message.toString();
				in = cr.openInputStream(Uri.parse(message.toString()));
				while ((len =in.read(buf) ) != -1) {
					out.write(buf, 0,len );
			}
				out.close();
				in.close();
				return null;
			} catch (FileNotFoundException e) {
				return null;
			} catch (IOException e) {
				return null;
			} finally {
				if (socket != null) {
					if (socket.isConnected()) {
						try {
							socket.close();
						} catch (IOException e) {
						}
					}
				}
			}
		}

	}
}
