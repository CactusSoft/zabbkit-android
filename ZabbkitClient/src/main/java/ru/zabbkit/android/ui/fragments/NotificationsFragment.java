package ru.zabbkit.android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.zabbkit.android.R;
import ru.zabbkit.android.push.GcmPrefUtil;

/**
 * Created by Alex.Shimborsky on 30/10/2014.
 */
public class NotificationsFragment extends Fragment {

    private static Fragment instance = null;

    public static Fragment newInstance() {
        if(instance == null) {
            instance = new NotificationsFragment();
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_notifications, container,
                false);

        String devId = GcmPrefUtil.readDevId(getActivity());
        if (devId == null) {
            boolean isGcmRegistrationFailed = GcmPrefUtil
                    .readRegistrationFail(getActivity());
            if (isGcmRegistrationFailed) {
                view.findViewById(R.id.warning).setVisibility(View.VISIBLE);
            } else {
                ((TextView) view.findViewById(R.id.reg_id))
                        .setText(getString(R.string.label_dev_id_not_detected));
            }
            view.findViewById(R.id.send_email).setEnabled(false);
        } else {
            ((TextView) view.findViewById(R.id.reg_id)).setText(devId);
            view.findViewById(R.id.send_email).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
                    intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(getString(
                            R.string.email_push_notif_text_format,
                            GcmPrefUtil.readDevId(getActivity()))));
                    intent.putExtra(Intent.EXTRA_SUBJECT,
                            getString(R.string.email_push_notif_subject));
                    startActivity(Intent.createChooser(intent,
                            getString(R.string.btn_send_email_instructions)));
                }
            });
        }

        return view;
    }
}
