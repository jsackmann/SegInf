package com.example.lallamaquenollama;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	private int uploaded;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		uploaded = 0;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void mandarRequest(View view) {
		File sdCardRoot = Environment.getExternalStorageDirectory();
		File f = new File(sdCardRoot, "/DCIM/prueba");
		File[] files = f.listFiles();
		for (File inFile : files) {
			if (inFile.isFile() && inFile.getName().matches("(.*)\\.jpg$")) {
				Log.d("LALLAMA",inFile.getName());
				new ImageUploadTask().execute(inFile);
			}
		}
	}

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private void showData(String data) {
		uploaded++;
		TextView p1_button = (TextView) findViewById(R.id.unLabel);
		p1_button.setText("Archivos Subidos :" + uploaded);
	}

	private class ImageUploadTask extends AsyncTask<File, Integer, String> {
		@Override
		protected String doInBackground(File... params) {
			// TODO Auto-generated method stub
			return postImage(params[0]);
		}

		protected void onPostExecute(String result) {
			showData(result);
		}

		public String postImage(File f) {
			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"http://manzana.no-ip.org/poster.php");

			try {
				// Add your data
//				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				InputStreamEntity reqEntity = new InputStreamEntity(
						new FileInputStream(f), -1);
				reqEntity.setContentType("application/octet-stream");
				reqEntity.setChunked(true); // Send in multiple parts if needed
				httppost.setEntity(reqEntity);

//				nameValuePairs.add(new BasicNameValuePair("file", f.getName()));
//
//				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				String res = convertStreamToString(response.getEntity()
						.getContent());

				Log.d("LALLAMA", res);
				return res;
			} catch (Exception e) {
				Log.d("LALLAMA", "Fallo el post: " + Log.getStackTraceString(e));
				return null;
			}
		}
	}

	private class GetTask extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			return getData();
		}

		protected void onPostExecute(String result) {
			showData(result);
		}

		public String getData() {
			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet get = new HttpGet("http://manzana.no-ip.org/archivo.txt");

			try {
				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(get);
				String res = convertStreamToString(response.getEntity()
						.getContent());
				return res;
			} catch (Exception e) {
				Log.d("LALLAMA", "Fallo el get: " + Log.getStackTraceString(e));
				return null;
			}
		}
	}
}
