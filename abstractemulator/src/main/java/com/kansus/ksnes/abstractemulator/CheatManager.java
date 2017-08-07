package com.kansus.ksnes.abstractemulator;

import java.util.List;

/**
 * Defines operations needed by an emulator's cheat manager.
 *
 * Created by Charles Nascimento on 03/08/2017.
 */
public interface CheatManager {

    List<Cheat> getAll();

    void setModified();

    Cheat add(String code, String name);

    void remove(int i);

    void enable(int i, boolean enabled);

    void save();

    void destroy();
}
