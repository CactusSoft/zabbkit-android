package ru.zabbkit.android.utils;

import ru.zabbkit.android.app.Constants;

/**
 * Created by Alex.Shimborsky on 29/10/2014.
 */
public class StringUtils {

    private StringUtils() {

    }

    public static String checkURL(String url) {
        if (url.toLowerCase().startsWith(Constants.URL_CHECK_HTTP) ||
                url.toLowerCase().startsWith(Constants.URL_CHECK_HTTPS)) {
            return url;
        } else {
            return Constants.URL_CHECK_HTTP + url.toLowerCase();
        }
    }
}
