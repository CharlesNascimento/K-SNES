package com.kansus.ksnes.preferences;

/**
 * 
 * Created by charl on 30/08/2017.
 */
public interface PreferencesManager {

    String getString(String key, String defaultValue);

    int getInt(String key, int defaultValue);

    long getLong(String key, long defaultValue);

    float getFloat(String key, float defaultValue);

    boolean getBoolean(String key, boolean defaultValue);
}
