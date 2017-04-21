package ru.zabbkit.android.manager;

import com.google.android.gms.wearable.DataMap;

import java.util.ArrayList;

/**
 * Created by Alex.Shimborsky on 23/12/2014.
 */
public class DataManager {

    private static DataManager instance;

    public ArrayList<DataMap> triggersArray;

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

}
