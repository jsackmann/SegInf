package com.example.lallamaquenollama;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpVersion;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.mime.HttpMultipartMode;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntity;
import ch.boye.httpclientandroidlib.entity.mime.content.FileBody;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.params.CoreProtocolPNames;
import ch.boye.httpclientandroidlib.util.EntityUtils;

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
			HttpPost httppost = new HttpPost("http://manzana.no-ip.org/poster.php");
			HttpClient client = new DefaultHttpClient();

			client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			 
			MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
			
			FileBody fb = new FileBody(f);
			entity.addPart( "imageName", fb);
			 			 
			httppost.setEntity( entity );
			 
			try {
				return EntityUtils.toString( client.execute( httppost ).getEntity(), "UTF-8" );
			} catch (Exception e) {
				Log.d("LALLAMA", "Fallo el post: " + Log.getStackTraceString(e));
				return null;
			}finally{
				client.getConnectionManager().shutdown();
			}
		}
	}

	@SuppressWarnings("unused")
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
