package ru.zabbkit.android.utils;

import android.content.Context;

import java.io.File;
import java.io.InputStream;

import ru.zabbkit.android.R;

public class SSLResource implements ru.zabbkitserver.android.remote.utils.SSLResource {

	private Context mContext;

	public SSLResource(Context ctx) {
		mContext = ctx;
	}

	@Override
	public InputStream getTrustStore() {
		return mContext.getResources().openRawResource(R.raw.mytruststore);
	}

	@Override
	public InputStream getKeyStore() {
		return mContext.getResources().openRawResource(R.raw.mykeystore);
	}

	@Override
	public File getBKSFile() {
		File file = new File(mContext.getFilesDir(), "mytruststore.bks");
		return file;
	}

}
