package ru.zabbkit.android.utils;

import java.util.List;

import ru.zabbkitserver.android.remote.request.AsyncRequestListener;

/**
 * Created by Dmitry.Kalyuzhnyi on 11/03/2014.
 */
public interface LoginRequestListener extends AsyncRequestListener {
    public void onRequestSuccess(String url, List<Object> result, Class<?> clazz);
}
