package com.kansus.ksnes.input.source;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.kansus.ksnes.abstractemulator.input.InputSourceListener;
import com.kansus.ksnes.abstractemulator.input.source.SensorInputSource;
import com.kansus.ksnes.input.SNESControlKeys;

public class SensorKeypad implements SensorInputSource, SensorEventListener {

    private static final int[] SENSOR_MAP_DPAD = {
            SNESControlKeys.GAMEPAD_LEFT,
            SNESControlKeys.GAMEPAD_RIGHT,
            SNESControlKeys.GAMEPAD_UP,
            SNESControlKeys.GAMEPAD_DOWN,
    };

    private static final int[] SENSOR_MAP_TRIGGERS = {
            SNESControlKeys.GAMEPAD_TL,
            SNESControlKeys.GAMEPAD_TR,
            0,
            0
    };

    @SuppressWarnings("PointlessBitwiseExpression")
    private static final int LEFT = (1 << 0);
    private static final int RIGHT = (1 << 1);
    private static final int UP = (1 << 2);
    private static final int DOWN = (1 << 3);

    private static final float THRESHOLD_VALUES[] = {
            30.0f, 20.0f, 15.0f, 10.0f, 8.0f,
            6.0f, 5.0f, 3.0f, 2.0f, 1.0f,
    };

    private Context context;
    private InputSourceListener mInputSourceListener;

    private int[] sensorMappings = SENSOR_MAP_DPAD;
    private int keyStates;
    private float threshold = THRESHOLD_VALUES[7];

    public SensorKeypad(Context ctx) {
        context = ctx;
    }

    public void setInputSourceListener(InputSourceListener listener) {
        if (mInputSourceListener == listener)
            return;

        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (mInputSourceListener != null)
            sensorManager.unregisterListener(this);

        mInputSourceListener = listener;
        if (mInputSourceListener != null) {
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public final int getKeyStates() {
        return keyStates;
    }

    @Override
    public final void setSensitivity(int value) {
        if (value < 0)
            value = 0;
        else if (value > 9)
            value = 9;

        threshold = THRESHOLD_VALUES[value];
    }

    @Override
    public void reset() {

    }

    @Override
    public void disable() {

    }

    @Override
    public void enable() {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float leftRight, upDown;

        Configuration config = context.getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            leftRight = -event.values[1];
            upDown = event.values[2];
        } else {
            leftRight = -event.values[2];
            upDown = -event.values[1];
        }

        int states = 0;
        if (leftRight < -threshold)
            states |= LEFT;
        else if (leftRight > threshold)
            states |= RIGHT;

        if (upDown < -threshold)
			states |= UP;
		else if (upDown > threshold)
			states |= DOWN;

        if (states != keyStates) {
            keyStates = convertToMappedKeys(states);
            if (mInputSourceListener != null)
                mInputSourceListener.onKeyStatesChanged();
        }
    }

    // FIXME Method not called
    private int[] getSensorMappings(String as) {
        if ("dpad".equals(as))
            sensorMappings = SENSOR_MAP_DPAD;
        if ("triggers".equals(as))
            sensorMappings = SENSOR_MAP_TRIGGERS;
        return null;
    }

    private int convertToMappedKeys(int keys) {
        int states = 0;

        if ((keys & SensorKeypad.LEFT) != 0)
            states |= sensorMappings[0];
        if ((keys & SensorKeypad.RIGHT) != 0)
            states |= sensorMappings[1];
        if ((keys & SensorKeypad.UP) != 0)
            states |= sensorMappings[2];
        if ((keys & SensorKeypad.DOWN) != 0)
            states |= sensorMappings[3];

        return states;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void clearKeyMap() {

    }

    @Override
    public void mapKey(int originalKey, int newKey) {

    }
}
