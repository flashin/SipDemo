package com.gedevanishvili.sipdemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;

public class DemoSip {
	private String domain = "sip.online.ge";
	private String username = "2000211";
	private String password = "wXeHi4RfC2";
	private SipManager mSipManager = null;
	private SipProfile mSipProfile = null;
	private SipAudioCall call;
	private Context context;
	private boolean isRegistered = false;
	private String myError = "";

	public DemoSip(Context cont) {
		context = cont;

		if (mSipManager == null) {
			mSipManager = SipManager.newInstance(context);
		}

		try {
			SipProfile.Builder builder = new SipProfile.Builder(username,
					domain);

			builder.setPassword(password);
			builder.setDisplayName(username);
			builder.setProfileName(username + "@" + domain);
			builder.setProtocol("UDP");
			builder.setPort(5060);
			builder.setOutboundProxy(domain);
			mSipProfile = builder.build();

			Intent intent = new Intent();
			intent.setAction("android.SipDemo.INCOMING_CALL");
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
					0, intent, Intent.FILL_IN_DATA);

			mSipManager.open(mSipProfile, pendingIntent, null);

			mSipManager.setRegistrationListener(mSipProfile.getUriString(),
					new SipRegistrationListener() {

						public void onRegistering(String localProfileUri) {
							updateStatusWithAlert("Registering with SIP Server...");
						}

						public void onRegistrationDone(String localProfileUri,
								long expiryTime) {
							updateStatus("Ready");
						}

						public void onRegistrationFailed(
								String localProfileUri, int errorCode,
								String errorMessage) {
							updateStatusWithAlert("Registration failed.  Please check settings. Code: "
									+ errorCode + "; Message: " + errorMessage);
						}
					});
		} catch (Exception e) {
			MyAlert.alertWin(context, e.toString() + " - " + myError);
		}
	}

	/**
	 * start a call
	 */
	public void initCall(String phone) {

		if (isRegistered == false) {
			MyAlert.alertWin(context,
					"Not connected yet, please try again later");
			return;
		}

		SipAudioCall.Listener listener = new SipAudioCall.Listener() {
			@Override
			public void onCallEstablished(SipAudioCall call) {
				// TODO Auto-generated method stub
				super.onCallEstablished(call);
				updateStatus("Call Established");
				call.startAudio();
				call.setSpeakerMode(true);
				call.toggleMute();
			}

			@Override
			public void onCallEnded(SipAudioCall call) {
				// TODO Auto-generated method stub
				super.onCallEnded(call);
			}

			@Override
			public void onCalling(SipAudioCall call) {
				super.onCalling(call);

				updateStatus("Call started");
			}

			@Override
			public void onError(SipAudioCall call, int errorCode,
					String errorMessage) {

				super.onError(call, errorCode, errorMessage);
				updateStatusWithAlert("Call error code: " + errorCode
						+ "; Message: " + errorMessage);
				updateStatus("Call Error");
			}
		};
		try {
			SipProfile.Builder builder = new SipProfile.Builder(phone, domain);
			SipProfile peerProfile = builder.build();
			call = mSipManager.makeAudioCall(mSipProfile.getUriString(),
					peerProfile.getUriString(), listener, 30);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			MyAlert.alertWin(context, e.toString());
		}
	}

	/**
	 * End call
	 */
	public void endCall() {
		if (call != null) {
			try {
				call.endCall();
			} catch (Exception e) {
				MyAlert.alertWin(context, e.toString());
			}
			call = null;
		}
	}

	public void updateStatus(final String status) {

		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (status.equals("Ready") == true && isRegistered == false) {
					MyAlert.alertWin(context, "Connection established");
					isRegistered = true;
				} else if (status.equals("Call started") == true) {
					MyAlert.alertWinWithDisconnect(context, "Calling...");
				} else if (status.equals("Call Established") == true) {
					MyAlert.dismissDialog();
					MyAlert.alertWinWithDisconnect(context, "Call established");
				} else if (status.equals("Call Error") == true) {
					MyAlert.dismissDialog();
					MyAlert.alertWinWithDisconnect(context, "Call failed");
				}
			}

		});

	}

	public void updateStatusWithAlert(final String status) {

		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				MyAlert.alertWin(context, status);
			}

		});

	}

};
