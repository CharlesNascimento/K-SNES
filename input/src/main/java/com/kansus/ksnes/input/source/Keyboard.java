package com.kansus.ksnes.input.source;

import android.view.KeyEvent;
import android.view.View;

import com.kansus.ksnes.abstractemulator.input.InputSourceListener;
import com.kansus.ksnes.abstractemulator.input.source.InputSource;


public class Keyboard implements InputSource, View.OnKeyListener {

    //private static final String LOG_TAG = "Keyboard";

    private InputSourceListener mInputSourceListener;
    private int[] keysMap = new int[128];
    private int keyStates;

    public Keyboard(View view) {
        view.setOnKeyListener(this);
    }

    public final int getKeyStates() {
        return keyStates;
    }

    public void setInputSourceListener(InputSourceListener listener) {
        mInputSourceListener = listener;
    }

    public void reset() {
        keyStates = 0;
    }

    @Override
    public void disable() {

    }

    @Override
    public void enable() {

    }

    public void clearKeyMap() {
        for (int i = 0; i < keysMap.length; i++)
            keysMap[i] = 0;
    }

    public void mapKey(int originalKey, int newKey) {
        if (newKey >= 0 && newKey < keysMap.length)
            keysMap[newKey] |= originalKey;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode >= keysMap.length)
            return false;

        int gameKey = keysMap[keyCode];
        if (gameKey != 0) {
            if (event.getRepeatCount() == 0) {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    keyStates |= gameKey;
                else
                    keyStates &= ~gameKey;

                mInputSourceListener.onKeyStatesChanged();
            }
            return true;
        }
        return false;
    }
}
