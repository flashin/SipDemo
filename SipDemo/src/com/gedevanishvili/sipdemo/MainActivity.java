package com.gedevanishvili.sipdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private DemoSipBango DemoSip = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (DemoSip == null) {
			DemoSip = new DemoSipBango(this);

			try {
				DemoSip.init();
			} catch (Exception e) {
				MyAlert.alertWin(this, e.getMessage());
			}
		}
	}

	/**
	 * Tap the number
	 * 
	 * @param view
	 */
	public void tapTheNumber(View view) {
		Button but = (Button) view;

		String str = but.getText().toString();

		TextView tv = (TextView) findViewById(R.id.number_container);

		str = tv.getText().toString() + str;
		if (str.length() < 13) {
			tv.setText(str);
		}
	}

	/**
	 * Clear number plate
	 */
	public void clearNumberPlate(View view) {

		TextView tv = (TextView) findViewById(R.id.number_container);
		tv.setText("");

		DemoSip.endCall();
	}

	/**
	 * Disconnect
	 */
	public void disconnectCall() {
		try {
			DemoSip.endCall();
		} catch (Exception e) {
			MyAlert.alertWin(this, e.toString());
		}
	}

	/**
	 * Start call
	 */
	public void startCall(View view) {

		TextView tv = (TextView) findViewById(R.id.number_container);
		String number = tv.getText().toString();

		try {
			DemoSip.initCall(number);
		} catch (Exception e) {
			MyAlert.alertWin(this, e.toString());
		}
	}

	/**
	 * Start call
	 */
	public void ChangeSipStatus(String status) {

		TextView tv = (TextView) findViewById(R.id.status_cont);
		tv.setText("Status: " + status);
	}
}
