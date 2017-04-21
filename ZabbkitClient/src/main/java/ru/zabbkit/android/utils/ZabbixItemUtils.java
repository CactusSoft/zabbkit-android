package ru.zabbkit.android.utils;

import android.support.v4.util.ArrayMap;
import android.text.format.DateFormat;

import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.zabbkit.android.app.Constants;
import ru.zabbkitserver.android.remote.model.ZabbixItem;

/**
 * @author Dmitry.Kalenchuk
 */
public final class ZabbixItemUtils {

    private static final String ATTR_REGEX = "\\$\\d+";
    private static final Pattern STTR_PATTERN = Pattern.compile(ATTR_REGEX);

    private ZabbixItemUtils() {
    }

    public static String parseItemName(ZabbixItem item) {
        String itemName = item.getName();
        // parse item key
        // key is presented in next format: <keyId>[<keyArg1>, <keyArg2>, ...]
        final String keyString = item.getKey();
        String[] keys = keyString.substring(keyString.indexOf('[') + 1,
                keyString.lastIndexOf(']')).split(",");

        ArrayMap<String, String> argToValueMap = new ArrayMap<String, String>();

        final Matcher argMatcher = STTR_PATTERN.matcher(itemName);
        while (argMatcher.find()) {
            final String argument = itemName.substring(argMatcher.start(),
                    argMatcher.end());
            final String argumentNumber = argument.replace("$", "");
            // arguments starts from 1
            final String value = keys[Integer.valueOf(argumentNumber) - 1];
            argToValueMap.put(argument, value);
        }

        for (Entry<String, String> entry : argToValueMap.entrySet()) {
            itemName = itemName
                    .replace(entry.getKey(), entry.getValue().trim());
        }

        return itemName;
    }

    public static String parseItemValue(ZabbixItem item) {
        String valueString;

        if (item.getValueType() == ZabbixItem.VALUE_TYPE_UINT
                || item.getValueType() == ZabbixItem.VALUE_TYPE_FLOAT) {
            final double value = Float.valueOf(item.getLastValue())
                    * item.getFormula();
            final String unit = item.getUnits().trim();

            if ("uptime".equals(unit)) {
                valueString = TimeParser.parseTime((long) value);
            } else if ("unixtime".equals(unit)) {
                valueString = DateFormat.format(Constants.DATE_FORMAT,
                        (long) value * Constants.MS_IN_SEC).toString();
            } else if ("B".equals(unit) || "bps".equals(unit)) {
                if (item.getValueType() == ZabbixItem.VALUE_TYPE_UINT) {
                    valueString = ValueConvertUtils.convertInteger(
                            (long) value, unit);
                } else {
                    valueString = ValueConvertUtils.convertFloat(value, unit);
                }
            } else {
                if (item.getValueType() == ZabbixItem.VALUE_TYPE_UINT) {
                    valueString = String.format(Locale.ENGLISH, "%d %s",
                            (long) value, unit);
                } else {
                    valueString = String.format(Locale.ENGLISH, "%.3f %s",
                            value, unit);
                }
            }
        } else {
            valueString = item.getLastValue();
        }

        return valueString;
    }
}
