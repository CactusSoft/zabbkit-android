package ru.zabbkit.android.ui.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.zabbkit.android.R;
import ru.zabbkit.android.utils.L;

public class AboutCastFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.ac_about_program_cast, container,
				false);
        TextView mVersionProgram = (TextView) view.findViewById(
                R.id.view_version);

        try {
            PackageInfo pInfo = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0);
            final String versionValue = String.format(
                    getString(R.string.version_template), pInfo.versionName);
            mVersionProgram.setText(versionValue);
        } catch (NameNotFoundException e) {
            L.e(e);
        }
		return view;
	}
}
