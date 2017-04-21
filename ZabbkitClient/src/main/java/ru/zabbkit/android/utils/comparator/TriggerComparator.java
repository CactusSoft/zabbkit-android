package ru.zabbkit.android.utils.comparator;

import java.util.Comparator;

import ru.zabbkit.android.app.Constants;
import ru.zabbkitserver.android.remote.model.Trigger;

/**
 * Created by Alex.Shimborsky on 08/12/2014.
 */
public class TriggerComparator implements Comparator<Trigger> {
    @Override
    public int compare(Trigger lhs, Trigger rhs) {
        int c;
        c = rhs.getValue().compareTo(lhs.getValue());
        if (c == 0 && Constants.STATE_OK.equals(lhs.getValue())) {
            c = lhs.getDescription().compareTo(rhs.getDescription());
        } else if (c == 0) {
            c = rhs.getPriority().compareTo(lhs.getPriority());
        }
        if (c == 0) {
            c = lhs.getDescription().compareTo(rhs.getDescription());
        }
        return c;
    }
}
