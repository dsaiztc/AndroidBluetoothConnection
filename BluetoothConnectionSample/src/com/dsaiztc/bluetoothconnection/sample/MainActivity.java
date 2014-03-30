package com.dsaiztc.bluetoothconnection.sample;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.dsaiztc.bluetoothconnection.BluetoothConnection;

public class MainActivity extends Activity implements OnClickListener
{
	private static final String TAG = "MainActivity";

	private Button mConnectButton;
	private Button mAskForButton;
	private Button mStopAskingButton;
	private Button mDisconnectButton;

	private BluetoothConnection bc;

	private ScheduledExecutorService mScheduledExecutorService;
	
	private boolean asking = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Set up the window layout
		setContentView(R.layout.main);

		mConnectButton = (Button) this.findViewById(R.id.button_connect);
		mConnectButton.setOnClickListener(this);

		mAskForButton = (Button) this.findViewById(R.id.button_ask_for);
		mAskForButton.setOnClickListener(this);

		mStopAskingButton = (Button) this.findViewById(R.id.button_stop_asking_for);
		mStopAskingButton.setOnClickListener(this);

		mDisconnectButton = (Button) this.findViewById(R.id.button_disconnect);
		mDisconnectButton.setOnClickListener(this);

		bc = new BluetoothConnection(this);

		mScheduledExecutorService = Executors.newScheduledThreadPool(5);
		mScheduledExecutorService.scheduleAtFixedRate(new Runnable()
		{
			public void run()
			{
				if (asking)
				{
					askFor();
					Log.i(TAG, "askFor()");
				}
			}
		}, 0L, 10L, TimeUnit.SECONDS);

	}

	@Override
	public void onStart()
	{
		super.onStart();

		bc.onStart();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		bc.onDestroy();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.i(TAG, "onActivityResult");
		bc.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case (R.id.button_connect):
				Log.i(TAG, "button_connect");
				bc.scanForDevices(BluetoothConnection.REQUEST_CONNECT_DEVICE_SECURE);
				break;
			case (R.id.button_ask_for):
				Log.i(TAG, "button_ask_for");
				asking = true;
				break;
			case (R.id.button_stop_asking_for):
				Log.i(TAG, "button_stop_asking_for");
				asking = false;
				break;
			case (R.id.button_disconnect):
				Log.i(TAG, "button_disconnect");
				bc.onDestroy();
				break;
		}
	}

	private void askFor()
	{
		bc.sendMessage("BA1");
	}
}
