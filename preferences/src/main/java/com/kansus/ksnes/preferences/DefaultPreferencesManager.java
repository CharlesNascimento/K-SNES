package com.kansus.ksnes.preferences;

import android.content.SharedPreferences;

/**
 * The default preferences manager. It uses the Android
 * {@link android.content.SharedPreferences} as the underlying
 * preferences engine.
 * <p>
 * Created by Charles Nascimento on 09/09/2017.
 */
public class DefaultPreferencesManager implements PreferencesManager {

    private SharedPreferences mSharedPreferences;

    public DefaultPreferencesManager(SharedPreferences sharedPreferences) {
        this.mSharedPreferences = sharedPreferences;
    }

    @Override
    public String getString(String key, String defaultValue) {
        return null;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return 0;
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return 0;
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return 0;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return false;
    }
}
