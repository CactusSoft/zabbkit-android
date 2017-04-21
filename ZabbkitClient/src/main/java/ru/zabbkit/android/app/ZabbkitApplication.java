package ru.zabbkit.android.app;

import android.app.Application;
import android.os.StrictMode;

import ru.zabbkit.android.utils.SSLResource;
import ru.zabbkit.android.utils.SharedPreferencesEditor;
import ru.zabbkitserver.android.remote.utils.SSLManager;

public class ZabbkitApplication extends Application {

    @Override
    public void onCreate() {
        if (AppConfig.DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll().penaltyLog().build());
        }

        super.onCreate();
        SharedPreferencesEditor.init(this);
        SSLResource sslResource = new SSLResource(this);
        SSLManager.init(sslResource);
    }

}
