package com.kansus.ksnes.input.source;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;

import com.kansus.ksnes.abstractemulator.input.InputSourceListener;
import com.kansus.ksnes.abstractemulator.input.source.TouchInputSource;
import com.kansus.ksnes.input.SNESControlKeys;
import com.kansus.ksnes.input.R;

import java.util.ArrayList;

public class VirtualKeypad implements TouchInputSource {

    private static final int DPAD_4WAY[] = {
            SNESControlKeys.GAMEPAD_LEFT,
            SNESControlKeys.GAMEPAD_UP,
            SNESControlKeys.GAMEPAD_RIGHT,
            SNESControlKeys.GAMEPAD_DOWN
    };

    private static final int BUTTONS_4WAY[] = {
            SNESControlKeys.GAMEPAD_Y,
            SNESControlKeys.GAMEPAD_X,
            SNESControlKeys.GAMEPAD_A,
            SNESControlKeys.GAMEPAD_B
    };

    private static final float DPAD_DEADZONE_VALUES[] = {
            0.1f, 0.14f, 0.1667f, 0.2f, 0.25f,
    };

    private Context context;
    private View view;
    private float scaleX;
    private float scaleY;
    private int transparency;

    private InputSourceListener mInputSourceListener;
    private int keyStates;
    private Vibrator vibrator;
    private boolean vibratorEnabled;
    private boolean dpad4Way;
    private float dpadDeadZone = DPAD_DEADZONE_VALUES[2];
    private float pointSizeThreshold;
    //private boolean inBetweenPress;

    private ArrayList<Control> controls = new ArrayList<>();
    private Control dpad;
    private Control buttons;
    private Control selectStart;
    private Control leftShoulder;
    private Control rightShoulder;

    public VirtualKeypad(View v) {
        view = v;
        context = view.getContext();

        vibrator = (Vibrator) context.getSystemService(
                Context.VIBRATOR_SERVICE);

        dpad = createControl(R.drawable.dpad);
        buttons = createControl(R.drawable.buttons);
        selectStart = createControl(R.drawable.select_start_buttons);
        leftShoulder = createControl(R.drawable.tl_button_top);
        rightShoulder = createControl(R.drawable.tr_button_top);
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

    @Override
    public final void destroy() {
    }

    @Override
    public final void resize(int w, int h) {
        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences(context);
        vibratorEnabled = prefs.getBoolean("enableVibrator", true);
        dpad4Way = prefs.getBoolean("dpad4Way", false);

        int value = prefs.getInt("dpadDeadZone", 2);
        value = (value < 0 ? 0 : (value > 4 ? 4 : value));
        dpadDeadZone = DPAD_DEADZONE_VALUES[value];

        //inBetweenPress = prefs.getBoolean("inBetweenPress", false);

        pointSizeThreshold = 1.0f;
        if (prefs.getBoolean("pointSizePress", false)) {
            int threshold = prefs.getInt("pointSizePressThreshold", 7);
            pointSizeThreshold = (threshold / 10.0f) - 0.01f;
        }

        dpad.hide(prefs.getBoolean("hideDpad", false));
        buttons.hide(prefs.getBoolean("hideButtons", false));
        selectStart.hide(prefs.getBoolean("hideSelectStart", false));
        leftShoulder.hide(prefs.getBoolean("hideShoulders", false));
        rightShoulder.hide(prefs.getBoolean("hideShoulders", false));

        scaleX = (float) w / view.getWidth();
        scaleY = (float) h / view.getHeight();

        float controlScale = getControlScale(prefs);
        float sx = scaleX * controlScale;
        float sy = scaleY * controlScale;

        for (Control c : controls)
            c.load(context, sx, sy);

        reposition(w, h, prefs);

        transparency = prefs.getInt("vkeypadTransparency", 50);
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAlpha(transparency * 2 + 30);

        for (Control c : controls)
            c.draw(canvas, paint);
    }

    private static float getControlScale(SharedPreferences prefs) {
        String value = prefs.getString("vkeypadSize", null);
        if ("small".equals(value))
            return 1.0f;
        if ("large".equals(value))
            return 1.33333f;
        return 1.2f;
    }

    private Control createControl(int resId) {
        Control c = new Control(resId);
        controls.add(c);
        return c;
    }

    private void makeBottomBottom(int w, int h) {
        if (dpad.getWidth() + buttons.getWidth() > w) {
            makeBottomTop(w, h);
            return;
        }

        dpad.move(0, h - dpad.getHeight());
        buttons.move(w - buttons.getWidth(), h - buttons.getHeight());
        leftShoulder.move(0, 0);
        rightShoulder.move(w - rightShoulder.getWidth(), 0);

        int x = (w + dpad.getWidth() - buttons.getWidth() -
                selectStart.getWidth()) / 2;
        if (x > dpad.getWidth())
            selectStart.move(x, h - selectStart.getHeight());
        else {
            x = (w - selectStart.getWidth()) / 2;
            selectStart.move(x, 0);
        }
    }

    private void makeTopTop(int w, int h) {
        if (dpad.getWidth() + buttons.getWidth() > w) {
            makeBottomTop(w, h);
            return;
        }
        dpad.move(0, 0);
        buttons.move(w - buttons.getWidth(), 0);

        leftShoulder.reload(context, R.drawable.tl_button_bottom);
        rightShoulder.reload(context, R.drawable.tr_button_bottom);
        leftShoulder.move(0, h - leftShoulder.getHeight());
        rightShoulder.move(w - rightShoulder.getWidth(),
                h - rightShoulder.getHeight());

        selectStart.move((w - selectStart.getWidth()) / 2,
                h - selectStart.getHeight());
    }

    private void makeTopBottom(int w, int h) {
        dpad.move(0, 0);
        buttons.move(w - buttons.getWidth(), h - buttons.getHeight());

        leftShoulder.reload(context, R.drawable.tl_button_bottom);
        leftShoulder.move(0, h - leftShoulder.getHeight());
        rightShoulder.move(w - rightShoulder.getWidth(), 0);

        int x = (w + leftShoulder.getWidth() - buttons.getWidth() -
                selectStart.getWidth()) / 2;
        if (x > leftShoulder.getWidth())
            selectStart.move(x, h - selectStart.getHeight());
        else {
            x = (w + dpad.getWidth() - selectStart.getWidth()) / 2;
            selectStart.move(x, rightShoulder.getHeight());
        }
    }

    private void makeBottomTop(int w, int h) {
        dpad.move(0, h - dpad.getHeight());
        buttons.move(w - buttons.getWidth(), 0);

        rightShoulder.reload(context, R.drawable.tr_button_bottom);
        leftShoulder.move(0, 0);
        rightShoulder.move(w - rightShoulder.getWidth(),
                h - rightShoulder.getHeight());

        int x = (w + dpad.getWidth() - rightShoulder.getWidth() -
                selectStart.getWidth()) / 2;
        if (x > dpad.getWidth())
            selectStart.move(x, h - selectStart.getHeight());
        else {
            x = (w + dpad.getWidth() - selectStart.getWidth()) / 2;
            selectStart.move(x,
                    h - leftShoulder.getHeight() - selectStart.getHeight());
        }
    }

    private void reposition(int w, int h, SharedPreferences prefs) {
        String layout = prefs.getString("vkeypadLayout", "top_bottom");

        if ("top_bottom".equals(layout))
            makeTopBottom(w, h);
        else if ("bottom_top".equals(layout))
            makeBottomTop(w, h);
        else if ("top_top".equals(layout))
            makeTopTop(w, h);
        else
            makeBottomBottom(w, h);
    }

    private boolean shouldVibrate(int oldStates, int newStates) {
        return (((oldStates ^ newStates) & newStates) != 0);
    }

    private void setKeyStates(int newStates) {
        if (keyStates == newStates)
            return;

        if (vibratorEnabled && shouldVibrate(keyStates, newStates))
            vibrator.vibrate(33);

        keyStates = newStates;
        mInputSourceListener.onKeyStatesChanged();
    }

    private int get4WayDirection(float x, float y) {
        x -= 0.5f;
        y -= 0.5f;

        if (Math.abs(x) >= Math.abs(y))
            return (x < 0.0f ? 0 : 2);
        return (y < 0.0f ? 1 : 3);
    }

    private int getDpadStates(float x, float y) {
        if (dpad4Way)
            return DPAD_4WAY[get4WayDirection(x, y)];

        final float cx = 0.5f;
        final float cy = 0.5f;
        int states = 0;

        if (x < cx - dpadDeadZone)
            states |= SNESControlKeys.GAMEPAD_LEFT;
        else if (x > cx + dpadDeadZone)
            states |= SNESControlKeys.GAMEPAD_RIGHT;
        if (y < cy - dpadDeadZone)
            states |= SNESControlKeys.GAMEPAD_UP;
        else if (y > cy + dpadDeadZone)
            states |= SNESControlKeys.GAMEPAD_DOWN;

        return states;
    }

    private int getButtonsStates(float x, float y, float size) {
        int states = BUTTONS_4WAY[get4WayDirection(x, y)];

        if (size > pointSizeThreshold) {
            switch (states) {
                case SNESControlKeys.GAMEPAD_Y:
                case SNESControlKeys.GAMEPAD_B:
                    states = (SNESControlKeys.GAMEPAD_Y | SNESControlKeys.GAMEPAD_B);
                    break;
                case SNESControlKeys.GAMEPAD_X:
                case SNESControlKeys.GAMEPAD_A:
                    states = (SNESControlKeys.GAMEPAD_X | SNESControlKeys.GAMEPAD_A);
                    break;
            }
        }
        return states;
    }

    private int getSelectStartStates(float x, float y) {
        return (x < 0.5f ? SNESControlKeys.GAMEPAD_SELECT : SNESControlKeys.GAMEPAD_START);
    }

    private float getEventX(MotionEvent event, int index, boolean flip) {
        float x = event.getX(index);
        if (flip)
            x = view.getWidth() - x;
        return (x * scaleX);
    }

    private float getEventY(MotionEvent event, int index, boolean flip) {
        float y = event.getY(index);
        if (flip)
            y = view.getHeight() - y;
        return y * scaleY;
    }

    private Control findControl(float x, float y) {
        for (Control c : controls) {
            if (c.hitTest(x, y))
                return c;
        }
        return null;
    }

    private int getControlStates(Control c, float x, float y, float size) {
        x = (x - c.getX()) / c.getWidth();
        y = (y - c.getY()) / c.getHeight();

        if (c == dpad)
            return getDpadStates(x, y);
        if (c == buttons)
            return getButtonsStates(x, y, size);
        if (c == selectStart)
            return getSelectStartStates(x, y);
        if (c == leftShoulder)
            return SNESControlKeys.GAMEPAD_TL;
        if (c == rightShoulder)
            return SNESControlKeys.GAMEPAD_TR;

        return 0;
    }

    @Override
    public boolean fireTouch(Object data, boolean isScreenFlipped) {
        // Return if we haven't yet loaded bitmap resources
        if (dpad.bitmap == null)
            return false;

        MotionEvent event = (MotionEvent) data;
        int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setKeyStates(0);
                return true;

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_OUTSIDE:
                break;
            default:
                return false;
        }

        int states = 0;
        int n = event.getPointerCount();
        for (int i = 0; i < n; i++) {
            float x = getEventX(event, i, isScreenFlipped);
            float y = getEventY(event, i, isScreenFlipped);
            Control c = findControl(x, y);

            if (c != null) {
                states |= getControlStates(c, x, y, event.getSize(i));
            }
        }

        setKeyStates(states);
        return true;
    }

    @Override
    public void clearKeyMap() {

    }

    @Override
    public void mapKey(int originalKey, int newKey) {

    }

    private static class Control {

        private int resId;
        private boolean hidden;
        private boolean disabled;
        private Bitmap bitmap;
        private RectF bounds = new RectF();

        Control(int r) {
            resId = r;
        }

        final float getX() {
            return bounds.left;
        }

        final float getY() {
            return bounds.top;
        }

        final int getWidth() {
            return bitmap.getWidth();
        }

        final int getHeight() {
            return bitmap.getHeight();
        }

        final boolean isEnabled() {
            return !disabled;
        }

        final void hide(boolean b) {
            hidden = b;
        }

        final void disable(boolean b) {
            disabled = b;
        }

        final boolean hitTest(float x, float y) {
            return bounds.contains(x, y);
        }

        final void move(float x, float y) {
            bounds.set(x, y, x + bitmap.getWidth(), y + bitmap.getHeight());
        }

        final void load(Context context, float sx, float sy) {
            bitmap = ((BitmapDrawable) ContextCompat.getDrawable(context, resId)).getBitmap();
            bitmap = Bitmap.createScaledBitmap(bitmap,
                    (int) (sx * bitmap.getWidth()),
                    (int) (sy * bitmap.getHeight()), true);
        }

        final void reload(Context context, int id) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            bitmap = ((BitmapDrawable) ContextCompat.getDrawable(context, id)).getBitmap();
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
        }

        final void draw(Canvas canvas, Paint paint) {
            if (!hidden && !disabled)
                canvas.drawBitmap(bitmap, bounds.left, bounds.top, paint);
        }
    }
}
