package com.kansus.ksnes.input;

import android.content.Context;
import android.content.res.Configuration;
import android.view.KeyEvent;

public class DefaultPreferences {

    public static final int[] gameKeys = {
            SNESControlKeys.GAMEPAD_UP,
            SNESControlKeys.GAMEPAD_DOWN,
            SNESControlKeys.GAMEPAD_LEFT,
            SNESControlKeys.GAMEPAD_RIGHT,
            SNESControlKeys.GAMEPAD_UP_LEFT,
            SNESControlKeys.GAMEPAD_UP_RIGHT,
            SNESControlKeys.GAMEPAD_DOWN_LEFT,
            SNESControlKeys.GAMEPAD_DOWN_RIGHT,
            SNESControlKeys.GAMEPAD_SELECT,
            SNESControlKeys.GAMEPAD_START,
            SNESControlKeys.GAMEPAD_A,
            SNESControlKeys.GAMEPAD_B,
            SNESControlKeys.GAMEPAD_X,
            SNESControlKeys.GAMEPAD_Y,
            SNESControlKeys.GAMEPAD_TL,
            SNESControlKeys.GAMEPAD_TR,
    };

    public static final String[] gameKeysPref = {
            "gamepad_up",
            "gamepad_down",
            "gamepad_left",
            "gamepad_right",
            "gamepad_up_left",
            "gamepad_up_right",
            "gamepad_down_left",
            "gamepad_down_right",
            "gamepad_select",
            "gamepad_start",
            "gamepad_A",
            "gamepad_B",
            "gamepad_X",
            "gamepad_Y",
            "gamepad_TL",
            "gamepad_TR",
    };

    public static final String[] gameKeysPref2 = {
            "gamepad2_up",
            "gamepad2_down",
            "gamepad2_left",
            "gamepad2_right",
            "gamepad2_up_left",
            "gamepad2_up_right",
            "gamepad2_down_left",
            "gamepad2_down_right",
            "gamepad2_select",
            "gamepad2_start",
            "gamepad2_A",
            "gamepad2_B",
            "gamepad2_X",
            "gamepad2_Y",
            "gamepad2_TL",
            "gamepad2_TR",
    };

    private static final int keymaps_qwerty[] = {
            KeyEvent.KEYCODE_1,
            KeyEvent.KEYCODE_A,
            KeyEvent.KEYCODE_Q,
            KeyEvent.KEYCODE_W,
            0, 0, 0, 0,
            KeyEvent.KEYCODE_DEL,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_0,
            KeyEvent.KEYCODE_P,
            KeyEvent.KEYCODE_9,
            KeyEvent.KEYCODE_O,
            KeyEvent.KEYCODE_K,
            KeyEvent.KEYCODE_L,
    };

    private static final int keymaps_non_qwerty[] = {
            0, 0, 0, 0,
            0, 0, 0, 0,
            0,
            0,
            0,
            KeyEvent.KEYCODE_SEARCH,
            0,
            KeyEvent.KEYCODE_BACK,
            0, 0,
    };

    static {
        final int n = keymaps_qwerty.length;
        if (keymaps_non_qwerty.length != n)
            throw new AssertionError("Key configurations are not consistent");
    }

    private static boolean isKeyboardQwerty(Context context) {
        return (context.getResources().getConfiguration().keyboard ==
                Configuration.KEYBOARD_QWERTY);
    }

    private static boolean isNavigationDPad(Context context) {
        return (context.getResources().getConfiguration().navigation !=
                Configuration.NAVIGATION_TRACKBALL);
    }

    public static int[] getKeyMappings(Context context) {
        final int[] keymaps;

        if (isKeyboardQwerty(context))
            keymaps = keymaps_qwerty;
        else
            keymaps = keymaps_non_qwerty;

        if (isNavigationDPad(context)) {
            keymaps[0] = KeyEvent.KEYCODE_DPAD_UP;
            keymaps[1] = KeyEvent.KEYCODE_DPAD_DOWN;
            keymaps[2] = KeyEvent.KEYCODE_DPAD_LEFT;
            keymaps[3] = KeyEvent.KEYCODE_DPAD_RIGHT;
        }
        return keymaps;
    }
}
