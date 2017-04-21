package ru.zabbkit.android.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

import ru.zabbkit.android.R;
import ru.zabbkit.android.adapter.EventsAdapter;
import ru.zabbkit.android.manager.DataManager;
import ru.zabbkit.common.constants.ZabbkitConstants;

public class MainActivity extends Activity {

    private GoogleApiClient googleApiClient;
    private DataUpdateReceiver dataUpdateReceiver;
    private EventsAdapter eventsAdapter;

    private ArrayList<DataMap> triggersDataSet = new ArrayList<>();

    private RelativeLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingLayout = (RelativeLayout) findViewById(R.id.alert_layout);

        WearableListView listView =
                (WearableListView) findViewById(R.id.triggers_list);

        // Assign an adapter to the list
        eventsAdapter = new EventsAdapter(this, triggersDataSet);
        listView.setAdapter(eventsAdapter);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d("Error!", "onConnected: " + connectionHint);
                        sendTriggersRequest();
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d("Error!", "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d("Error!", "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();
        // Set a click listener
        //listView.setClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataUpdateReceiver == null) {
            dataUpdateReceiver = new DataUpdateReceiver();
        }
        IntentFilter intentFilter = new IntentFilter(ZabbkitConstants.BROADCAST_DATA_REQUEST);
        registerReceiver(dataUpdateReceiver, intentFilter);

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dataUpdateReceiver != null) {
            unregisterReceiver(dataUpdateReceiver);
        }
    }

    private void sendTriggersRequest() {
        if (googleApiClient.isConnected()) {
            PutDataMapRequest dataMapRequest = PutDataMapRequest.create(ZabbkitConstants.PATH_DATA_REQUEST);
            dataMapRequest.getDataMap().putDouble(ZabbkitConstants.TIMESTAMP, System.currentTimeMillis());
            //dataMapRequest.getDataMap().putString("type", "triggers");
            PutDataRequest putDataRequest = dataMapRequest.asPutDataRequest();
            Wearable.DataApi.putDataItem(googleApiClient, putDataRequest);
        } else {
            Log.e("Error!", "No connection to wearable available!");
        }
    }

    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ZabbkitConstants.BROADCAST_DATA_REQUEST)) {
                triggersDataSet = DataManager.getInstance().triggersArray;
                //if (triggersDataSet.size() > 0) {
                    loadingLayout.setVisibility(View.GONE);
                    eventsAdapter.setDataset(triggersDataSet);
                    eventsAdapter.notifyDataSetChanged();
                //} else {
                    //TODO: сообщение о пустом списке
                //}
            }
        }
    }

}
