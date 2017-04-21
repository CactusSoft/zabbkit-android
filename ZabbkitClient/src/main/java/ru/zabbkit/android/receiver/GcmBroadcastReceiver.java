package ru.zabbkit.android.receiver;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import ru.zabbkit.android.R;
import ru.zabbkit.android.push.GcmPrefUtil;
import ru.zabbkit.android.push.GcmUtil;
import ru.zabbkit.android.ui.activity.SlideMenuActivity;

/**
 * Receiver responsible for handling GCM messages and registration intents.
 * 
 * @author Sergey Tarasevich 17.07.2013
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {

	public static final String INTENT_ACTION_REGISTRATION = "com.google.android.c2dm.intent.REGISTRATION";

	public static final String MESSAGE_KEY_MESSAGE = "msg";
	public static final String MESSAGE_KEY_TRIGGER_ID = "triggerId";
	public static final String MESSAGE_KEY_SOUND = "sound";

	public static final int NOTIFICATION_ID = 0;

	public static final int WAKE_LOCK_TIMEOUT = 5000;

	@Override
	public void onReceive(final Context context, Intent intent) {
		if (INTENT_ACTION_REGISTRATION.equals(intent.getAction())) {
			boolean unexpectedIntent = !GcmPrefUtil
					.readWaitingForRegistration(context);
			if (unexpectedIntent) { // it seems Google refreshed registration ID
									// of the device, so we should to get new
									// reg ID
				GcmUtil.register(context,
						new GcmUtil.GcmRegistrationListener() {
							@Override
							public void onGcmRegistrationSucceed(String regId,
									String devId) {
								String ticker = context
										.getString(R.string.notif_reg_id_changed_ticker);
								String title = context
										.getString(R.string.notif_reg_id_changed_title);
								String text = context
										.getString(
												R.string.notif_reg_id_changed_text_format,
												devId);
								showNotification(context, ticker, title, text,
										true, NOTIFICATION_ID,
										SlideMenuActivity.class);
							}

							@Override
							public void onGcmRegistrationFailed(Exception e) { // Do
																				// nothing
							}
						});
			}
		} else {
			String messageType = GoogleCloudMessaging.getInstance(context)
					.getMessageType(intent);
			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				String title = context.getString(R.string.notif_title);
				String message = intent.getStringExtra(MESSAGE_KEY_MESSAGE);
				String triggerId = intent
						.getStringExtra(MESSAGE_KEY_TRIGGER_ID);
				String playSoundStr = intent.getStringExtra(MESSAGE_KEY_SOUND);
				boolean playSound = playSoundStr != null
						&& Boolean.parseBoolean(playSoundStr);
				assert triggerId != null;
				showNotification(context, message, title, message, playSound,
						triggerId.hashCode(), SlideMenuActivity.class);
			}
		}
		setResultCode(Activity.RESULT_OK);
	}

	private void showNotification(Context context, CharSequence ticker,
			CharSequence title, CharSequence text, boolean playSound,
			int notifId, Class<?> activity) {
		Intent intent = new Intent(context, activity);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context);
		builder.setTicker(ticker);
		builder.setContentTitle(title);
		builder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
		builder.setContentText(text);
		builder.setAutoCancel(true);
		builder.setContentIntent(contentIntent);
		builder.setSmallIcon(R.drawable.ic_stat_notify_push_message);
		if (playSound) {
			builder.setDefaults(Notification.DEFAULT_SOUND
					| Notification.DEFAULT_VIBRATE
					| Notification.DEFAULT_LIGHTS);
		}
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(notifId, builder.build());

		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP,
				GcmBroadcastReceiver.class.getSimpleName());
		wl.acquire(WAKE_LOCK_TIMEOUT);
	}
}