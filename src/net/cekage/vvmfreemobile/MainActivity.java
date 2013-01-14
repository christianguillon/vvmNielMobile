package net.cekage.vvmfreemobile;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * inspired by
 * http://code.google.com/p/krvarma-android-samples/source/browse/trunk
 * /SMSDemo/?r=37
 */

public class MainActivity extends Activity {
	public static final String TAG = "cekage_FreeVVM";
	EditText etlp;

	private String FREEMOBILE_ASKING = "isAsking";

	private String FREEMOBILE_DISPLAYED_DATE = "vvminfos";
	ImageButton ibSearch;
	ImageView ivLogo;
	ProgressBar pbAsking;

	private BroadcastReceiver smsreceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			SmsMessage[] msgs = null;

			if (null != bundle) {
				String info = "";// "Binary SMS from ";
				Object[] pdus = (Object[]) bundle.get("pdus");
				msgs = new SmsMessage[pdus.length];
				byte[] data = null;

				for (int i = 0; i < msgs.length; i++) {
					msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

					data = msgs[i].getUserData();

					for (int index = 0; index < data.length; ++index) {
						info += Character.toString((char) data[index]);
					}
				}
				updateETLP(info);
				uiAsking(false);
			}
		}

	};

	public void btnClickFetch(View v) {

		SmsManager smsm = SmsManager.getDefault();
		smsm.sendDataMessage("2051", null, (short) 5499,
				"STATUS:pv=13;ct=free.VVM.10;pt=5499;//VVM".getBytes(), null,
				null);
		uiAsking(true);

	}

	public void btnClickShare(View v) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, etlp.getText());
		sendIntent.setType("text/plain");
		startActivity(sendIntent);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		etlp = (EditText) findViewById(R.id.etLoginPass);
		ibSearch = (ImageButton) findViewById(R.id.btnSearch);
		ivLogo = (ImageView) findViewById(R.id.ivLogo);
		pbAsking = (ProgressBar) findViewById(R.id.pbAsking);
		if (savedInstanceState != null) {
			etlp.setText(savedInstanceState
					.getString(FREEMOBILE_DISPLAYED_DATE));
			uiAsking(savedInstanceState.getBoolean(FREEMOBILE_ASKING, false));
		}

		IntentFilter ifDATASMS = new IntentFilter(
				"android.intent.action.DATA_SMS_RECEIVED");
		ifDATASMS.addDataScheme("sms");
		registerReceiver(smsreceiver, ifDATASMS);

	}

	private void uiAsking(Boolean isAsking) {
		ivLogo.setVisibility(isAsking ? View.GONE : View.VISIBLE);
		pbAsking.setVisibility(!isAsking ? View.GONE : View.VISIBLE);
		ibSearch.setEnabled(!isAsking);
	}

	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putString(FREEMOBILE_DISPLAYED_DATE, etlp.getText().toString());
		bundle.putBoolean(FREEMOBILE_ASKING, !ibSearch.isEnabled());
	}

	private void updateETLP(String binarymessage) {

		String FREEMOBILE_LOGIN = "";
		String FREEMOBILE_PASSWORD = "";
		String FREEMOBILE_SERVER = "";
		String FREEMOBILE_PORT = "";

		if (binarymessage.matches(".*;u=.*;pw=.*;.*")) {

			String[] separated = binarymessage.split(";");
			for (String param : separated) {
				if (param.startsWith("u="))
					FREEMOBILE_LOGIN = param.substring(2);
				else if (param.startsWith("pw="))
					FREEMOBILE_PASSWORD = param.substring(3);
				else if (param.startsWith("srv="))
					FREEMOBILE_SERVER = param.substring(4);
				else if (param.startsWith("ipt="))
					FREEMOBILE_PORT = param.substring(4);
			}
			etlp.setText(String.format(
					"Login: %s\nPass: %s\nServer: %s\nPort: %s",
					FREEMOBILE_LOGIN, FREEMOBILE_PASSWORD, FREEMOBILE_SERVER,
					FREEMOBILE_PORT));

		} else {

			etlp.setText(binarymessage);
		}
	}

}
