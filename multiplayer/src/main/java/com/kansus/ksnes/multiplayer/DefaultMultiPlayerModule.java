package com.kansus.ksnes.multiplayer;

import com.kansus.ksnes.abstractemulator.multiplayer.MultiPlayerModule;

/**
 * The default input module used by the emulator.
 * <p>
 * Created by Charles Nascimento on 07/09/2017.
 */
public class DefaultMultiPlayerModule implements MultiPlayerModule {

    private boolean mIsEnabled;

    @Override
    public void init() {

    }

    @Override
    public void enable() {
        mIsEnabled = true;
    }

    @Override
    public void disable() {
        mIsEnabled = false;
    }

    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }
}
