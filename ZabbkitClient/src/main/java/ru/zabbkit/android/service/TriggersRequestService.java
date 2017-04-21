package ru.zabbkit.android.service;

import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.utils.Communicator;
import ru.zabbkit.android.utils.comparator.TriggerComparator;
import ru.zabbkit.common.constants.ZabbkitConstants;
import ru.zabbkitserver.android.remote.model.Trigger;
import ru.zabbkitserver.android.remote.request.AsyncRequestListener;

/**
 * @author Alex.Shimborsky on 11.01.2015.
 */
public class TriggersRequestService extends WearableListenerService
        implements AsyncRequestListener {

    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                if (ZabbkitConstants.PATH_DATA_REQUEST.equals(dataEvent.getDataItem().getUri().getPath())) {

                    Map<String, Object> params = new ArrayMap<String, Object>();

                    params.put(Constants.REQ_SELECT_HOSTS, new String[]{
                            Constants.REQ_VAL_HOST_ID, Constants.REQ_VAL_HOST});
                    params.put(Constants.REQ_SORT_FIELD, Constants.REQ_VAL_DESCRIPTION);
                    params.put(Constants.REQ_OUTPUT, Constants.REQ_VAL_EXTEND);
                    params.put(Constants.REQ_MONITORED, true);
                    params.put(Constants.REQ_EXPAND_DESCRIPTION, true);
                    Communicator.getInstance().getTriggers(params, this);

                }
            }
        }
    }

    @Override
    public void onRequestFailure(Exception e, String message) {
        Log.d("onRequestFailure", message);
    }

    @Override
    public void onRequestSuccess(List<Object> result, Class<?> clazz) {
        if (clazz == Trigger.class) {

            ArrayList<Trigger> triggersList = new ArrayList<>();
            for (Object object : result) {
                if (object instanceof Trigger) {
                    triggersList.add((Trigger) object);
                }
            }
            Collections.sort(triggersList, new TriggerComparator());

            if (googleApiClient.isConnected()) {
                PutDataMapRequest dataMapRequest = PutDataMapRequest.create(ZabbkitConstants.PATH_DATA_RESULT);
                dataMapRequest.getDataMap().putDouble(ZabbkitConstants.TIMESTAMP, System.currentTimeMillis());

                ArrayList<DataMap> dataMapArray = new ArrayList<>();
                for (Trigger trigger : triggersList) {
                    DataMap dataMap = new DataMap();
                    dataMap.putString(ZabbkitConstants.REQUEST_RESULT_DESCRIPTION, trigger.getDescription());
                    dataMap.putString(ZabbkitConstants.REQUEST_RESULT_PRIORITY, trigger.getPriority());
                    dataMapArray.add(dataMap);
                }
                dataMapRequest.getDataMap().putDataMapArrayList(ZabbkitConstants.REQUEST_RESULT_ARRAY, dataMapArray);
                PutDataRequest putDataRequest = dataMapRequest.asPutDataRequest();
                Wearable.DataApi.putDataItem(googleApiClient, putDataRequest);
            } else {
                Log.e("Error!", "No connection to wearable available!");
            }
        }
    }

    @Override
    public void onCertificateRequest(X509Certificate[] certificate) {

    }
}
