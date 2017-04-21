package ru.zabbkit.android.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import ru.zabbkit.android.app.Constants;

public final class SharedPreferencesEditor {

	private static final SharedPreferencesEditor INSTANCE = new SharedPreferencesEditor();

	private static SharedPreferences sPreferences;

	private SharedPreferences.Editor mEditor;

	private SharedPreferencesEditor() {
	}

	public static SharedPreferencesEditor getInstance() {
		return INSTANCE;
	}

	@SuppressLint("CommitPrefEdits")
	public static void init(Context ctx) {
		sPreferences = ctx.getSharedPreferences(Constants.PREFS_NAME, Activity.MODE_PRIVATE);
	}

	public void putString(String key, String value) {
		mEditor = sPreferences.edit();
		mEditor.putString(key, value);
		mEditor.apply();
	}

	public String getString(String key) {
		return sPreferences.getString(key, null);
	}

	public Boolean getBoolean(String key, boolean value) {
		return sPreferences.getBoolean(key, value);
	}

	public void removeValue(String key) {
		mEditor = sPreferences.edit();
		mEditor.remove(key);
		mEditor.apply();
	}

	public void putBoolean(String key, boolean value) {
		mEditor = sPreferences.edit();
		mEditor.putBoolean(key, value);
		mEditor.apply();
	}
}
