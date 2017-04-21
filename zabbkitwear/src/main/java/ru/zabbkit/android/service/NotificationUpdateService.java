package ru.zabbkit.android.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;

import ru.zabbkit.android.R;
import ru.zabbkit.android.manager.DataManager;
import ru.zabbkit.android.ui.activity.MainActivity;
import ru.zabbkit.common.constants.ZabbkitConstants;

import static com.google.android.gms.wearable.PutDataRequest.WEAR_URI_SCHEME;

/**
 * @author Sergey Tarasevich on 11.01.2015.
 */
public class NotificationUpdateService extends WearableListenerService {

    private int notificationId = 001;
    final static String GROUP_KEY = "group_key";


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            String action = intent.getAction();
            if (ZabbkitConstants.NOTIFICATION_DISMISS.equals(action)) {
                dismissNotification();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for(DataEvent dataEvent: dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                Log.d("type", dataEvent.getDataItem().getUri().getPath());
                if (ZabbkitConstants.PATH_NOTIFICATION.equals(dataEvent.getDataItem().getUri().getPath())) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
                    String title = dataMapItem.getDataMap().getString(ZabbkitConstants.NOTIFICATION_TITLE);
                    String content = dataMapItem.getDataMap().getString(ZabbkitConstants.NOTIFICATION_CONTENT);
                    sendNotification(title, content);
                } else if(ZabbkitConstants.PATH_DATA_RESULT.equals(dataEvent.getDataItem().getUri().getPath())){
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
                    ArrayList<DataMap> triggers = dataMapItem.getDataMap().
                            getDataMapArrayList(ZabbkitConstants.REQUEST_RESULT_ARRAY);
                    DataManager.getInstance().triggersArray = triggers;
                    sendBroadcast(new Intent(ZabbkitConstants.BROADCAST_DATA_REQUEST));
                }
            }
        }
    }

    private void sendNotification(String title, String content) {

        // this intent will open the activity when the user taps the "open" action on the notification
        Intent viewIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingViewIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);

        // this intent will be sent when the user swipes the notification to dismiss it
        Intent dismissIntent = new Intent(ZabbkitConstants.NOTIFICATION_DISMISS);
        PendingIntent pendingDeleteIntent = PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setGroup(GROUP_KEY)
                .setDeleteIntent(pendingDeleteIntent)
                .setContentIntent(pendingViewIntent);

        Notification notification = builder.build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId++, notification);
    }

    private void dismissNotification() {
        new DismissNotificationCommand(this).execute();
    }

    private class DismissNotificationCommand implements GoogleApiClient.ConnectionCallbacks, ResultCallback<DataApi.DeleteDataItemsResult>, GoogleApiClient.OnConnectionFailedListener {

        private static final String TAG = "DismissNotification";

        private final GoogleApiClient mGoogleApiClient;

        public DismissNotificationCommand(Context context) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        public void execute() {
            mGoogleApiClient.connect();
        }

        @Override
        public void onConnected(Bundle bundle) {
            final Uri dataItemUri =
                    new Uri.Builder().scheme(WEAR_URI_SCHEME).path(ZabbkitConstants.PATH_NOTIFICATION).build();
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Deleting Uri: " + dataItemUri.toString());
            }
            Wearable.DataApi.deleteDataItems(
                    mGoogleApiClient, dataItemUri).setResultCallback(this);
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d(TAG, "onConnectionSuspended");
        }

        @Override
        public void onResult(DataApi.DeleteDataItemsResult deleteDataItemsResult) {
            if (!deleteDataItemsResult.getStatus().isSuccess()) {
                Log.e(TAG, "dismissWearableNotification(): failed to delete DataItem");
            }
            mGoogleApiClient.disconnect();
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.d(TAG, "onConnectionFailed");
        }
    }
}