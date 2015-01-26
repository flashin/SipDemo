package com.gedevanishvili.sipdemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnUriUtils;

public class DemoSipBango {
	private String domain = "sip.domain";
	private String username = "sip.username";
	private String password = "sip.password";
	private int port = 5060;
	private Context context;
	private boolean isRegistered = false;

	private BroadcastReceiver mSipBroadCastRecv;
	private NgnAVSession avSession = null;

	private final NgnEngine mEngine;
	private final INgnConfigurationService mConfigurationService;
	private final INgnSipService mSipService;

	public DemoSipBango(Context cont) {
		context = cont;

		mEngine = NgnEngine.getInstance();
		mConfigurationService = mEngine.getConfigurationService();
		mSipService = mEngine.getSipService();
	}

	public void init() {
		// Listen for registration events
		mSipBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				// Registration Event
				if (NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT
						.equals(action)) {
					NgnRegistrationEventArgs args = intent
							.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
					if (args == null) {
						updateStatus("Invalid event args");
						return;
					}
					switch (args.getEventType()) {
					case REGISTRATION_NOK:
						updateStatus("Failed to register");
						break;
					case UNREGISTRATION_OK:
						updateStatus("You are now unregistered");
						break;
					case REGISTRATION_OK:
						updateStatus("You are now registered");
						isRegistered = true;
						break;
					case REGISTRATION_INPROGRESS:
						updateStatus("Trying to register...");
						break;
					case UNREGISTRATION_INPROGRESS:
						updateStatus("Trying to unregister...");
						break;
					case UNREGISTRATION_NOK:
						updateStatus("Failed to unregister");
						break;
					}
				}

				// Call Event
				if (NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)) {
					NgnInviteEventArgs args = intent
							.getParcelableExtra(NgnInviteEventArgs.EXTRA_EMBEDDED);
					if (args == null) {
						updateStatus("Invalid event args");
						return;
					}

					avSession = NgnAVSession.getSession(args.getSessionId());

					switch (args.getEventType()) {
					case INCOMING:
						updateStatus("incoming call");
						mEngine.getSoundService().startRingTone();
						MyAlert.alertIncomingCall(context, avSession);
						break;
					case INPROGRESS:
						updateStatus("call in progress");
						break;
					case RINGING:
						updateStatus("Ringing");
						break;
					case CONNECTED:
						updateStatus("Contected");
						mEngine.getSoundService().stopRingTone();
						mEngine.getSoundService().stopRingBackTone();
						break;
					case TERMWAIT:
						updateStatus("Terminating...");
						break;
					case TERMINATED:
						updateStatus("Terminated");
						mEngine.getSoundService().stopRingTone();
						mEngine.getSoundService().stopRingBackTone();
						break;
					case SIP_RESPONSE:
						updateStatus("Sip response");
						mEngine.getSoundService().startRingBackTone();
						break;
					}
				}
			}
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter
				.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
		intentFilter.addAction(NgnInviteEventArgs.ACTION_INVITE_EVENT);
		context.registerReceiver(mSipBroadCastRecv, intentFilter);

		// Starts the engine
		if (!mEngine.isStarted()) {
			if (mEngine.start()) {
				updateStatus("Engine started");
			} else {
				updateStatus("Failed to start the engine");
			}
		}

		// Register
		if (mEngine.isStarted()) {
			if (!mSipService.isRegistered()) {
				// Set credentials
				mConfigurationService.putString(
						NgnConfigurationEntry.IDENTITY_IMPI, username);
				mConfigurationService.putString(
						NgnConfigurationEntry.IDENTITY_IMPU,
						String.format("sip:%s@%s", username, domain));
				mConfigurationService.putString(
						NgnConfigurationEntry.IDENTITY_PASSWORD, password);
				mConfigurationService.putString(
						NgnConfigurationEntry.NETWORK_PCSCF_HOST, domain);
				mConfigurationService.putInt(
						NgnConfigurationEntry.NETWORK_PCSCF_PORT, port);
				mConfigurationService.putString(
						NgnConfigurationEntry.NETWORK_REALM, domain);
				mConfigurationService.putBoolean(
						NgnConfigurationEntry.NETWORK_USE_3G, true);
				mConfigurationService.putBoolean(
						NgnConfigurationEntry.NETWORK_USE_WIFI, true);
				mConfigurationService.putBoolean(
						NgnConfigurationEntry.GENERAL_VAD, false);
				// VERY IMPORTANT: Commit changes
				mConfigurationService.commit();
				// register (log in)
				mSipService.register(context);
			}
		}
	}

	/**
	 * start a call
	 */
	public void initCall(String phone) {
		if (!isRegistered) {
			MyAlert.alertWin(context, "Not registered yet");
		}

		final String validUri = NgnUriUtils.makeValidSipUri(String.format(
				"sip:%s@%s", phone, domain));
		if (validUri == null) {
			updateStatus("failed to normalize sip uri '" + phone + "'");
			return;
		}
		avSession = NgnAVSession.createOutgoingSession(
				mSipService.getSipStack(), NgnMediaType.Audio);

		avSession.makeCall(validUri);
	}

	/**
	 * End call
	 */
	public void endCall() {
		if (avSession != null) {
			avSession.hangUpCall();
		}
	}

	/**
	 * Update status
	 */
	public void updateStatus(final String status) {

		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((MainActivity) context).ChangeSipStatus(status);
			}

		});

	}
}
