package com.kansus.ksnes.input;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.kansus.ksnes.abstractemulator.input.InputListener;
import com.kansus.ksnes.abstractemulator.input.InputModule;
import com.kansus.ksnes.abstractemulator.input.InputSourceListener;
import com.kansus.ksnes.abstractemulator.input.source.InputSource;
import com.kansus.ksnes.abstractemulator.input.source.SensorInputSource;
import com.kansus.ksnes.abstractemulator.input.source.TouchInputSource;
import com.kansus.ksnes.input.source.Keyboard;
import com.kansus.ksnes.input.source.SensorKeypad;
import com.kansus.ksnes.input.source.VirtualKeypad;

/**
 * The default input module used by the emulator.
 * <p>
 * Created by Charles Nascimento on 27/08/2017.
 */
public class DefaultInputModule implements InputModule,
        InputSourceListener,
        View.OnTouchListener {

    private static final int GAMEPAD_LEFT_RIGHT = (SNESControlKeys.GAMEPAD_LEFT | SNESControlKeys.GAMEPAD_RIGHT);
    private static final int GAMEPAD_UP_DOWN = (SNESControlKeys.GAMEPAD_UP | SNESControlKeys.GAMEPAD_DOWN);
    private static final int GAMEPAD_DIRECTION = (GAMEPAD_UP_DOWN | GAMEPAD_LEFT_RIGHT);

    private boolean mIsEnabled = true;

    private InputSource mKeyboard;
    private TouchInputSource mVirtualKeypad;
    private SensorInputSource mInputSensor;

    private boolean mIsLightGunEnabled;
    private boolean mIsScreenFlipped;
    private boolean mIsTrackballEnabled;

    private int mTrackballSensitivity;

    private InputListener mInputListener;

    private View mRootView;
    private Rect mRenderingSurfaceRegion = new Rect();

    public DefaultInputModule(View v) {
        mRootView = v;
        mRootView.setOnTouchListener(this);

        mKeyboard = new Keyboard(v);
        mKeyboard.setInputSourceListener(this);
    }

    @Override
    public void setInputListener(InputListener inputListener) {
        mInputListener = inputListener;
    }

    @Override
    public void onKeyStatesChanged() {
        int states = mKeyboard.getKeyStates();

        if (mInputSensor != null) {
            states |= mInputSensor.getKeyStates();
        }

        if (mIsScreenFlipped)
            states = flipGameKeys(states);

        if (mVirtualKeypad != null)
            states |= mVirtualKeypad.getKeyStates();

        // resolve conflict keys
        if ((states & GAMEPAD_LEFT_RIGHT) == GAMEPAD_LEFT_RIGHT)
            states &= ~GAMEPAD_LEFT_RIGHT;
        if ((states & GAMEPAD_UP_DOWN) == GAMEPAD_UP_DOWN)
            states &= ~GAMEPAD_UP_DOWN;

        mInputListener.onKeyStatesChanged(states);
    }

    public void loadKeyBindings(SharedPreferences prefs) {
        Context context = mRootView.getContext();

        final int[] gameKeys = DefaultPreferences.gameKeys;
        final int[] defaultKeys = DefaultPreferences.getKeyMappings(context);
        mKeyboard.clearKeyMap();

        String[] gameKeysPref = DefaultPreferences.gameKeysPref;
        for (int i = 0; i < gameKeysPref.length; i++) {
            mKeyboard.mapKey(gameKeys[i], prefs.getInt(gameKeysPref[i], defaultKeys[i]));
        }

        gameKeysPref = DefaultPreferences.gameKeysPref2;
        for (int i = 0; i < gameKeysPref.length; i++) {
            mKeyboard.mapKey(gameKeys[i] << 16, prefs.getInt(gameKeysPref[i], 0));
        }

        mKeyboard.mapKey(SNESControlKeys.GAMEPAD_SUPERSCOPE_TURBO, prefs.getInt("gamepad_superscope_turbo", 0));
        mKeyboard.mapKey(SNESControlKeys.GAMEPAD_SUPERSCOPE_PAUSE, prefs.getInt("gamepad_superscope_pause", 0));
        mKeyboard.mapKey(SNESControlKeys.GAMEPAD_SUPERSCOPE_CURSOR, prefs.getInt("gamepad_superscope_cursor", 0));
    }

    @Override
    public InputSource getKeyboard() {
        return mKeyboard;
    }

    @Override
    public SensorInputSource getInputSensor() {
        return mInputSensor;
    }

    @Override
    public TouchInputSource getVirtualKeypad() {
        return mVirtualKeypad;
    }

    @Override
    public void reset() {
        mKeyboard.reset();

        if (mVirtualKeypad != null) {
            mVirtualKeypad.reset();
        }
    }

    @Override
    public void setScreenFlipped(boolean screenFlipped) {
        mIsScreenFlipped = screenFlipped;
    }

    @Override
    public void disable() {
        if (!mIsEnabled) {
            return;
        }

        mInputSensor.disable();
        mKeyboard.disable();
        mVirtualKeypad.disable();

        mIsEnabled = false;
    }

    @Override
    public void enable() {
        if (mIsEnabled) {
            return;
        }

        mInputSensor.enable();
        mKeyboard.enable();
        mVirtualKeypad.enable();

        mIsEnabled = true;
    }

    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    private int flipGameKeys(int keys) {
        int newKeys = (keys & ~GAMEPAD_DIRECTION);

        if ((keys & SNESControlKeys.GAMEPAD_LEFT) != 0) {
            newKeys |= SNESControlKeys.GAMEPAD_RIGHT;
        }

        if ((keys & SNESControlKeys.GAMEPAD_RIGHT) != 0) {
            newKeys |= SNESControlKeys.GAMEPAD_LEFT;
        }

        if ((keys & SNESControlKeys.GAMEPAD_UP) != 0) {
            newKeys |= SNESControlKeys.GAMEPAD_DOWN;
        }

        if ((keys & SNESControlKeys.GAMEPAD_DOWN) != 0) {
            newKeys |= SNESControlKeys.GAMEPAD_UP;
        }

        return newKeys;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mVirtualKeypad != null) {
            return mVirtualKeypad.fireTouch(event, mIsScreenFlipped);
        }

        if (mIsLightGunEnabled && event.getAction() == MotionEvent.ACTION_DOWN) {
            int surfaceWidth = mRenderingSurfaceRegion.width();
            int surfaceHeight = mRenderingSurfaceRegion.height();

            int x = (int) event.getX() * surfaceWidth / mRootView.getWidth();
            int y = (int) event.getY() * surfaceHeight / mRootView.getHeight();

            if (mIsScreenFlipped) {
                x = surfaceWidth - x;
                y = surfaceHeight - y;
            }

            if (mRenderingSurfaceRegion.contains(x, y)) {
                x -= mRenderingSurfaceRegion.left;
                y -= mRenderingSurfaceRegion.top;
                mInputListener.onFireLightGun(x, y);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTrackball(MotionEvent event) {
        float dx = event.getX();
        float dy = event.getY();

        if (mIsScreenFlipped) {
            dx = -dx;
            dy = -dy;
        }

        int duration1 = (int) (dx * mTrackballSensitivity);
        int duration2 = (int) (dy * mTrackballSensitivity);
        int key1 = 0;
        int key2 = 0;

        if (duration1 < 0) {
            key1 = SNESControlKeys.GAMEPAD_LEFT;
        } else if (duration1 > 0) {
            key1 = SNESControlKeys.GAMEPAD_RIGHT;
        }

        if (duration2 < 0) {
            key2 = SNESControlKeys.GAMEPAD_UP;
        } else if (duration2 > 0) {
            key2 = SNESControlKeys.GAMEPAD_DOWN;
        }

        if (key1 == 0 && key2 == 0)
            return false;

        mInputListener.onTrackball(key1, Math.abs(duration1), key2, Math.abs(duration2));
        return true;
    }

    @Override
    public void setLightGunEnabled(boolean enabled) {
        mIsLightGunEnabled = enabled;
    }

    @Override
    public void setTrackballEnabled(boolean enabled) {
        mIsTrackballEnabled = enabled;
    }

    @Override
    public void setSensorInputSourceEnabled(boolean enabled) {
        if (enabled) {
            if (mInputSensor == null) {
                mInputSensor = new SensorKeypad(mRootView.getContext());
                mInputSensor.setInputSourceListener(this);
            }
        } else {
            mInputSensor = null;
        }
    }

    @Override
    public void setTouchInputSourceEnabled(boolean enabled) {
        if (enabled) {
            mVirtualKeypad = new VirtualKeypad(mRootView);
            mVirtualKeypad.setInputSourceListener(this);
        } else {
            if (mVirtualKeypad != null) {
                mVirtualKeypad.destroy();
                mVirtualKeypad = null;
            }
        }
    }

    @Override
    public void setTrackballSensitivity(int sensitivity) {
        mTrackballSensitivity = sensitivity;
    }

    @Override
    public void notifyFrameDrawn(Canvas canvas) {
        if (getVirtualKeypad() != null) {
            getVirtualKeypad().draw(canvas);
        }
    }

    @Override
    public void setRenderingSurfaceRegion(Rect rect) {
        mRenderingSurfaceRegion = rect;
    }
}