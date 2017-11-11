package com.kansus.ksnes.abstractemulator.cheats;

import com.kansus.ksnes.abstractemulator.Module;

import java.util.List;

/**
 * Defines operations needed by an emulator's cheat module.
 * <p>
 * Created by Charles Nascimento on 03/08/2017.
 */
public interface CheatsModule extends Module {

    /**
     * Sets the ROM name.
     *
     * @param rom The ROM name.
     */
    void setROM(String rom);

    /**
     * Adds a new cheat to the list of cheats.
     *
     * @param code The code of the cheat.
     * @param name The name of the cheat.
     * @return The newly created Cheat object.
     */
    Cheat add(String code, String name);

    /**
     * Removes the cheat in the i position of the cheat's list.
     *
     * @param i The position of the cheat in the cheat's list.
     */
    void remove(int i);

    /**
     * Returns a list with all the currently loaded cheats.
     *
     * @return A list with all the currently loaded cheats.
     */
    List<Cheat> getAll();

    /**
     * Sets the cheat at the i position of the cheat's list as
     * enabled or disabled.
     *
     * @param i       The position of the cheat in the cheat's list.
     * @param enabled Whether the cheat must be enabled or disabled.
     */
    void setCheatEnabled(int i, boolean enabled);

    /**
     * Saves the list of cheats to a persistent storage.
     */
    void save();

    /**
     * Sets the list of cheats as modified.
     */
    void setModified();
}
