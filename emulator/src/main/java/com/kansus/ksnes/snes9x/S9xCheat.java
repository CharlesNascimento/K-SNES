package com.kansus.ksnes.snes9x;

import com.kansus.ksnes.abstractemulator.Cheat;

public class S9xCheat extends Cheat {

    String code;
    String name;
    boolean enabled;

    @Override
    public String toString() {
        if (name == null)
            return code;
        return name + "\n" + code;
    }
}