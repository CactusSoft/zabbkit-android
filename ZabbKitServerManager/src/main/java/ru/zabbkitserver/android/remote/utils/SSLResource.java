package ru.zabbkitserver.android.remote.utils;

import java.io.File;
import java.io.InputStream;

public interface SSLResource {

	InputStream getTrustStore();
	
	InputStream getKeyStore();
	
	File getBKSFile();
}
