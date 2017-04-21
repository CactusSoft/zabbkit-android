package ru.zabbkit.android.utils;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;

public final class GeneralAbility {

    private GeneralAbility() {
    }

    /**
     * @param value - value of trigger(or event) priority
     * @return integer value of appropriate drawable resource
     */
    public static int getTriggersResourceColor(int value) {
        int result;
        switch (value) {
            case Constants.STATE_NOT_CLASSIFIED:
                result = R.color.not_classified_color;
                break;
            case Constants.STATE_INFORMATION:
                result = R.color.information_color;
                break;
            case Constants.STATE_WARNING:
                result = R.color.warning_color;
                break;
            case Constants.STATE_AVERAGE:
                result = R.color.average_color;
                break;
            case Constants.STATE_HIGH:
                result = R.color.high_color;
                break;
            case Constants.STATE_DISASTER:
                result = R.color.disaster_color;
                break;
            default:
                result = R.color.not_classified_color;
                break;
        }

        return result;
    }

    /**
     * @param threatDegree - degree of event threat
     * @return name of threat
     */
    public static int getState(int threatDegree) {
        int result;
        switch (threatDegree) {
            case Constants.STATE_NOT_CLASSIFIED:
                result = R.string.not_classified;
                break;
            case Constants.STATE_INFORMATION:
                result = R.string.information;
                break;
            case Constants.STATE_WARNING:
                result = R.string.warning;
                break;
            case Constants.STATE_AVERAGE:
                result = R.string.average;
                break;
            case Constants.STATE_HIGH:
                result = R.string.high;
                break;
            case Constants.STATE_DISASTER:
                result = R.string.disaster;
                break;
            default:
                result = R.string.not_classified;
                break;
        }
        return result;
    }
}
