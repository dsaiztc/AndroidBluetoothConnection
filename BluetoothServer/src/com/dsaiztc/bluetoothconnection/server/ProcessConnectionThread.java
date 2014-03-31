package com.dsaiztc.bluetoothconnection.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.microedition.io.StreamConnection;

public class ProcessConnectionThread implements Runnable
{
	private final StreamConnection mConnection;
	private InputStream mInStream;
	private OutputStream mOutStream;
	
	private Serializable mSerializable;

	public ProcessConnectionThread(StreamConnection connection)
	{
		mConnection = connection;
		
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		
		try
		{
			tmpIn = mConnection.openDataInputStream();
			tmpOut = mConnection.openDataOutputStream();
		}
		catch (IOException e)
		{
			System.out.println("temp sockets not created" + e);
		}
		mInStream = tmpIn;
		mOutStream = tmpOut;
	}

	@Override
	public void run()
	{
		try
		{
			// prepare to receive data
			System.out.println("Conected: waiting for input\n-----------------------------\n");
			
			String readMessage;

			byte[] buffer = new byte[1024];
			int bytes;

			while (true)
			{
				try
				{
					bytes = mInStream.read(buffer);
					readMessage = new String(buffer, 0, bytes);
					System.out.println("Dispositivo : " + readMessage);
					
					switch(readMessage)
					{
						case "BA1":
							System.out.println("Servidor : " + "Petición recibida");
							sendData(mSerializable);
							break;
						default:
							System.out.println("Servidor : " + "NOT A VALID COMMAND");
							break;
					}
				}
				catch (IOException e)
				{
					System.out.println("No se puede leer del socket");
					//e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void sendData(Serializable serializable)
	{
		try
		{
			byte[] bytes = toByte(serializable);
			
			mOutStream.write(bytes);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private byte[] toByte(Object object)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		byte[] bytes = null;
		
		try
		{
			out = new ObjectOutputStream(bos);
			out.writeObject(object);
			bytes = bos.toByteArray();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (out != null)
				{
					out.close();
				}
			}
			catch (IOException ex)
			{
				// ignore close exception
			}
			try
			{
				bos.close();
			}
			catch (IOException ex)
			{
				// ignore close exception
			}
		}
		return bytes;
	}
}
