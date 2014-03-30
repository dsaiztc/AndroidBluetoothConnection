package com.dsaiztc.bluetoothconnection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class BluetoothSampleActivity extends Activity
{
	// Debugging
	private static final String TAG = "BluetoothSampleActivity";
	private static final boolean D = true;

	// Layout Views
	private ListView mConversationView;
	private EditText mOutEditText;
	private Button mSendButton;

	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;

	BluetoothConnection bc;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (D) Log.i(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		setContentView(R.layout.main);

		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
		mConversationView = (ListView) findViewById(R.id.in);
		mConversationView.setAdapter(mConversationArrayAdapter);

		// Initialize the compose field with a listener for the return key
		mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		mOutEditText.setOnEditorActionListener(mWriteListener);

		// Initialize the send button with a listener that for click events
		mSendButton = (Button) findViewById(R.id.button_send);
		mSendButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				// Send a message using content of the edit text widget
				TextView view = (TextView) findViewById(R.id.edit_text_out);
				String message = view.getText().toString();
				bc.sendMessage(message);
			}
		});

		bc = new BluetoothConnection(this);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if (D) Log.i(TAG, "++ ON START ++");

		bc.onStart();
	}

	@Override
	public synchronized void onResume()
	{
		super.onResume();
		if (D) Log.i(TAG, "+ ON RESUME +");
	}

	@Override
	public synchronized void onPause()
	{
		super.onPause();
		if (D) Log.i(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if (D) Log.i(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (D) Log.i(TAG, "-- ON DESTROY --");

		bc.onDestroy();
	}

	// The action listener for the EditText widget, to listen for the return key
	private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener()
	{
		public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
		{
			// If the action is a key-up event on the return key, send the message
			if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP)
			{
				String message = view.getText().toString();
				bc.sendMessage(message);
			}
			if (D) Log.i(TAG, "END onEditorAction");
			return true;
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (D) Log.i(TAG, "onActivityResult " + resultCode);
		bc.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		if (itemId == R.id.secure_connect_scan)
		{
			return bc.scanForDevices(BluetoothConnection.REQUEST_CONNECT_DEVICE_SECURE);
		}
		else if (itemId == R.id.insecure_connect_scan)
		{
			return bc.scanForDevices(BluetoothConnection.REQUEST_CONNECT_DEVICE_INSECURE);
		}
		else if (itemId == R.id.discoverable)
		{
			// Ensure this device is discoverable by others
			// ensureDiscoverable();
			return true;
		}
		return false;
	}
}
