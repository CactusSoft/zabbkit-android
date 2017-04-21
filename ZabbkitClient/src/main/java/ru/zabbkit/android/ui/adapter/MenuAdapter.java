package ru.zabbkit.android.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import ru.zabbkit.android.R;

/**
 * Created by Alex.Shimborsky on 26/11/2014.
 */
public class MenuAdapter extends ArrayAdapter<String> {

    private Context context;
    private String[] items = null;
    private int resource;

    public MenuAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
        items = context.getResources().getStringArray(R.array.menu_items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = convertView;
        if(row == null) {
            row = inflater.inflate(resource, parent, false);
        }

        TextView txtTitle = (TextView) row.findViewById(R.id.drawer_item_text);

        if (items != null && items.length > position) {
            txtTitle.setText(items[position]);
            switch (position) {
                case 0:
                    txtTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_drawer_overview, 0, 0, 0);
                    break;
                case 1:
                    txtTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_drawer_bookmarks, 0, 0, 0);
                    break;
                case 2:
                    txtTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_drawer_notifications, 0, 0, 0);
                    break;
                case 3:
                    txtTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_server, 0, 0, 0);
                    break;
                case 4:
                    txtTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_drawer_about, 0, 0, 0);
                    break;
                case 5:
                    txtTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_drawer_logout, 0, 0, 0);
                    break;
            }
        }

        return row;
    }

    @Override
    public int getCount() {
        return items.length;
    }

}
