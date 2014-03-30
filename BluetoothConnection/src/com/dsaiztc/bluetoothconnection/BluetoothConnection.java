package com.dsaiztc.bluetoothconnection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * Class for Bluetooth connection management
 * 
 * Some of this methods must be called from the mActivity that uses it
 * 
 * @author Daniel Saiz Llarena
 * 
 */
public class BluetoothConnection
{
	// Debugging
	private static final String TAG = "BluetoothConnection";
	private static final boolean D = true;

	// Message types sent from the BluetoothService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	public static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothService mService = null;

	// Context of the principal Activity
	private Context mContext;
	// Principal Activity
	private Activity mActivity;

	/**
	 * Constructor Context of the activity
	 * 
	 * @param context
	 */
	public BluetoothConnection(Context context)
	{
		if (D) Log.i(TAG, "constructor");

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		mContext = context;
		mActivity = (Activity) mContext;

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null)
		{
			Toast.makeText(mContext.getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
			mActivity.finish();
			return;
		}
	}

	/**
	 * Method called on onStar() method of the principal Activity
	 * 
	 * Request for enabling Bluetooth and then instanciates the BluetoothService against setupChat()
	 * 
	 * Enables Bluetooth if necessary
	 */
	public void onStart()
	{
		if (D) Log.i(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled())
		{
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			mActivity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

		}
		else
		// Otherwise, setup the chat session
		{
			if (mService == null) setupChat();
		}
	}

	/**
	 * Method called on onDestroy method of principal Activity Stops the Bluetooth Service
	 */
	public void onDestroy()
	{
		if (D) Log.i(TAG, "--- ON DESTROY ---");

		// Stop the Bluetooth chat services
		if (mService != null) mService.stop();
	}

	/**
	 * Method that must be called on onActivityResult method of the principal Activity
	 * 
	 * @param requestCode Type of connection (secure or insecure) or enabling Bluetooth
	 * @param resultCode
	 * @param data Intent that has been returned
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (D) Log.i(TAG, "onActivityResult " + resultCode);
		switch (requestCode)
		{
			case REQUEST_CONNECT_DEVICE_SECURE:
				// When DeviceListActivity returns with a device to connect
				if (resultCode == Activity.RESULT_OK)
				{
					connectDevice(data, true);
				}
				break;
			case REQUEST_CONNECT_DEVICE_INSECURE:
				// When DeviceListActivity returns with a device to connect
				if (resultCode == Activity.RESULT_OK)
				{
					connectDevice(data, false);
				}
				break;
			case REQUEST_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK)
				{
					// Bluetooth is now enabled, so set up a chat session
					setupChat();
				}
				else
				{
					// User did not enable Bluetooth or an error occurred
					Log.d(TAG, "BT not enabled");
					Toast.makeText(mContext.getApplicationContext(), R.string.bt_not_enabled_leaving, Toast.LENGTH_LONG).show();
					mActivity.finish();
				}
		}
	}

	/**
	 * Setup connection: initialize the Bluetooth Service
	 */
	private void setupChat()
	{
		if (D) Log.i(TAG, "setupChat()");

		// Initialize the BluetoothService to perform bluetooth connections
		mService = new BluetoothService(mHandler);
	}

	/**
	 * Start DeviceListActivity to select/scan devices
	 * 
	 * @param request The type of connection: REQUEST_CONNECT_DEVICE_SECURE or REQUEST_CONNECT_DEVICE_INSECURE (< Bluetooth 2.1)
	 * @return true if it can be done, false other
	 */
	public boolean scanForDevices(int request)
	{
		if (D) Log.i(TAG, "list");

		Intent serverIntent = null;
		if (request == REQUEST_CONNECT_DEVICE_SECURE)
		{
			if (D) Log.i(TAG, "list: secure");
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(mContext, DeviceListActivity.class);
			mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			return true;
		}
		else if (request == REQUEST_CONNECT_DEVICE_INSECURE)
		{
			if (D) Log.i(TAG, "list: insecure");
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(mContext, DeviceListActivity.class);
			mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
			return true;
		}
		return false;
	}

	/**
	 * Sends a message.
	 * 
	 * @param message The text message that will be sended
	 */
	public void sendMessage(String message)
	{
		if (D) Log.e(TAG, "sendMessage");

		if (mService == null) return; // Puesto que se ha eliminado el finish() en onActivityResult(...) hay que comprobarlo
		// Check that we're actually connected before trying anything
		if (mService.getState() != BluetoothService.STATE_CONNECTED)
		{
			Toast.makeText(mContext.getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0)
		{
			// Get the message bytes and tell the BluetoothService to write
			byte[] send = message.getBytes();
			mService.write(send);
		}
	}

	/**
	 * Connect to the picked device
	 * 
	 * @param data Intent that has been returned
	 * @param secure Type of connection (secure -true- or insecure -false-)
	 */
	private void connectDevice(Intent data, boolean secure)
	{
		if (D) Log.i(TAG, "connectDevice");
		// Get the device MAC address
		String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BluetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mService.connect(device, secure);
	}

	/**
	 * Return the current connection state.
	 * 
	 * @return STATE_NONE = 0; STATE_LISTEN = 1; STATE_CONNECTING = 2; STATE_CONNECTED = 3;
	 */
	public int getState()
	{
		return mService.getState();
	}

	// The Handler that gets information back from the BluetoothService (and their threads)
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case MESSAGE_STATE_CHANGE:
					if (D) Log.i(TAG, "mHandler: MESSAGE_STATE_CHANGE: " + msg.arg1);
					switch (msg.arg1)
					{
						case BluetoothService.STATE_CONNECTED:
							if (D) Log.i(TAG, "mHandler: MESSAGE_STATE_CONNECTED");
							// Toast.makeText(mActivity.getApplicationContext(), R.string.title_connected_to, Toast.LENGTH_SHORT).show();
							Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.title_connected_to) + " "
									+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
							break;
						case BluetoothService.STATE_CONNECTING:
							if (D) Log.i(TAG, "mHandler: MESSAGE_STATE_CONNECTING");
							Toast.makeText(mActivity.getApplicationContext(), R.string.title_connecting, Toast.LENGTH_SHORT).show();
							break;
						case BluetoothService.STATE_LISTEN:
							if (D) Log.i(TAG, "mHandler: MESSAGE_STATE_LISTEN");
						case BluetoothService.STATE_NONE:
							if (D) Log.i(TAG, "mHandler: MESSAGE_STATE_NONE");
							Toast.makeText(mContext.getApplicationContext(), R.string.title_not_connected, Toast.LENGTH_SHORT).show();
							break;
					}
					break;
				case MESSAGE_WRITE:
					if (D) Log.i(TAG, "mHandler: MESSAGE_WRITE");
					// byte[] writeBuf = (byte[]) msg.obj;
					// construct a string from the buffer (if it's necessary to know what I'm sending for other issues)
					// String writeMessage = new String(writeBuf);
					break;
				case MESSAGE_READ:
					if (D) Log.i(TAG, "mHandler: MESSAGE_READ");
					byte[] readBuf = (byte[]) msg.obj;
					byte[] bytes = new byte[msg.arg1];

					for (int i = 0; i < msg.arg1; i++)
					{
						bytes[i] = readBuf[i];
					}

					// construct a string from the valid bytes in the buffer
					// String readMessage = new String(readBuf, 0, msg.arg1);
					try
					{
						Object object_received = toObject(bytes);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				case MESSAGE_DEVICE_NAME:
					if (D) Log.i(TAG, "mHandler: MESSAGE_DEVICE_NAME");
					// save the connected device's name
					mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
					// Toast.makeText(mContext.getApplicationContext(), "Conectado a " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
					break;
				case MESSAGE_TOAST:
					if (D) Log.i(TAG, "mHandler: MESSAGE_TOAST");
					int error = msg.getData().getInt(TOAST);
					switch (error)
					{
						case BluetoothService.UNABLE_TO_CONNECT:
							Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.unable_to_conect), Toast.LENGTH_SHORT).show();
							break;
						case BluetoothService.CONNECTION_LOST:
							Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.connection_lost), Toast.LENGTH_SHORT).show();
							break;
					}
					break;
			}
		}
	};

	public Object toObject(byte[] bytes)
	{
		Log.i(TAG, "toObject");

		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = null;
		Object o = null;
		try
		{
			in = new ObjectInputStream(bis);
			o = in.readObject();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				bis.close();
			}
			catch (IOException ex)
			{
				// ignore close exception
			}
			try
			{
				if (in != null)
				{
					in.close();
				}
			}
			catch (IOException ex)
			{
				// ignore close exception
			}
		}
		return o;
	}
}
