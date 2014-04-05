# AndroidBluetoothConnection

---------------------------------------------------------------------------------------------

Trying to understand a little bit more about how Bluetooth protocol works on Android devices, I build that simple Android library. 

It is based on [Android official documentation](http://developer.android.com/guide/topics/connectivity/bluetooth.html) (and also based on their example: [Bluetooth Chat](https://android.googlesource.com/platform/development/+/25b6aed7b2e01ce7bdc0dfa1a79eaf009ad178fe/samples/BluetoothChat)).

This repository is composed by 3 diferent projects:

- [BluetoothConnection](https://github.com/dsaiztc/AndroidBluetoothConnection/tree/master/BluetoothConnection): Bluetooth simple library for Android devices.
- [BluetoothConnectionSample](https://github.com/dsaiztc/AndroidBluetoothConnection/tree/master/BluetoothConnectionSample): Simple application to show how the library must be implemented/used in any Android code.
- [BluetoothServer](https://github.com/dsaiztc/AndroidBluetoothConnection/tree/master/BluetoothServer): Windows Bluetooth Server (to test the app or build your own system/functionality).

## BluetoothConnection
Simple Android Bluetooth library for adding Bluetooth capabilities to your application. This library is composed by the following elements:

- *BluetoothConnection* provides all methods for Bluetooth connection.
- *BluetoothService* manages configuration and management of Bluetooth conections. It has a thread to make connections and other thread for establised connections.
- *DeviceListActivity* show the user all Bluetooth devices availables. It could be not used if the Bluetooth connection has been done by pairing the Bluetooth device against Android Settings.

## BluetoothConnectionSample
For implementing Bluetooth connection on your application you have to configure some aspects. I will suppose that the IDE is Eclipse.

- First of all, you must import *BluetoothConnection* project to your *workspace*.
- Once it has been imported, make sure that it has been configured as *library* (go to *Project*->*Properties* (or *Alt+Enter*), in *Android* make sure *Is library* is checked).
- Now you have to add the project as library to your current project (that you want to add bluetooth capabilities). Go to *Project*->*Properties* (or *Alt+Enter*), in *Android* press *Add...* button and select *BluetoothConnection*.
- If you want to use *DeviceListActivity* you need to add it to your *AndroidManifest.xml* file. To do that, introduce a new *activity* like this:

````
<activity android:name="com.dsaiztc.bluetoothconnection.DeviceListActivity" 
			 android:configChanges="orientation|keyboardHidden" 
			 android:label="@string/select_device" 
			 android:theme="@android:style/Theme.Holo.Dialog" />
````

- Moreover, in your principal *Activity* you have to add a *BluetoothConnection* atribute, with your *Activity context* as input parameter. You must *override* some methods in this *Activity*. Those methods are:

````
onStart() 
onDestroy() 
onActivityResult(...)
````

I give you an example for clarify that:

````
@Override public void onStart() 
{ 
	super.onStart(); 
	myBluetoothConnection.onStart();
}
````

Once these stuff has been done, the main methods you will probably use are:

````
scanForDevices(int request) // Starts bluetooth devices list view
sendMessage(String message) // Send a message against bluetooth connection
````

Received messages are threated by a *Handler* in *BluetoothConnection*. If you want to finish the conection, just make a call to *onDestroy()* method of *BluetoothConnection*. If you have any doubts, please take a look to the sample code.


## BluetoothServer
That Bluetooth server is based on [Luu Gia Thuy web: Simple Android and Java Bluetooth Application](http://luugiathuy.com/2011/02/android-java-bluetooth/) (little modifications has been done).

It uses a Windows Java library for Bluetooth communication called [Bluecove](http://bluecove.org/), you can download the *jar* file *bluecove-2.1.1-SNAPSHOT.jar* on the [download site](http://snapshot.bluecove.org/distribution/download/2.1.1-SNAPSHOT/2.1.1-SNAPSHOT.62/) and import it to your project.

As I used it, the server waits for a connection from the phone, and when the connection has been done, it waits for receiving whatever you code to process.

------------------------------------------------------------------------------------------------

# About the repository
Please, feel free to modify whatever you want to improve this repository. I just "adapt" the original sample code from Google, so it wan't to be a perfect implementation or achieve the best Bluetooth performance connection.

If you consider that is the worst implementation you have ever seen, please let me know, I will be pleased to learn how to improve my code or how to do things better.

Thanks in advance to all.