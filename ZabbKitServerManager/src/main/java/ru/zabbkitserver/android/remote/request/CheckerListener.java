package ru.zabbkitserver.android.remote.request;

/**
 * Created by Alex.Shimborsky on 16/10/2014.
 */
public interface CheckerListener {

    void onSuccess(String code, String message);

    void onFailed(Exception e, String message);

    void onFailed(int code, String message, Object result, Class clazz);
}
