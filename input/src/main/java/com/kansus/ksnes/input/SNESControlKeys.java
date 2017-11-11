package com.kansus.ksnes.input;

/**
 * TEMP
 *
 * Created by charl on 03/08/2017.
 */
public class SNESControlKeys {

    public static final int GAMEPAD_TR = (1 << 4);
    public static final int GAMEPAD_TL = (1 << 5);
    public static final int GAMEPAD_X = (1 << 6);
    public static final int GAMEPAD_A = (1 << 7);
    public static final int GAMEPAD_RIGHT = (1 << 8);
    public static final int GAMEPAD_LEFT = (1 << 9);
    public static final int GAMEPAD_DOWN = (1 << 10);
    public static final int GAMEPAD_UP = (1 << 11);
    public static final int GAMEPAD_START = (1 << 12);
    public static final int GAMEPAD_SELECT = (1 << 13);
    public static final int GAMEPAD_Y = (1 << 14);
    public static final int GAMEPAD_B = (1 << 15);

    @SuppressWarnings("PointlessBitwiseExpression")
    public static final int GAMEPAD_SUPERSCOPE_TURBO = (1 << 0);
    public static final int GAMEPAD_SUPERSCOPE_PAUSE = (1 << 1);
    public static final int GAMEPAD_SUPERSCOPE_CURSOR = (1 << 2);

    public static final int GAMEPAD_UP_LEFT = (GAMEPAD_UP | GAMEPAD_LEFT);
    public static final int GAMEPAD_UP_RIGHT = (GAMEPAD_UP | GAMEPAD_RIGHT);
    public static final int GAMEPAD_DOWN_LEFT = (GAMEPAD_DOWN | GAMEPAD_LEFT);
    public static final int GAMEPAD_DOWN_RIGHT = (GAMEPAD_DOWN | GAMEPAD_RIGHT);
}
