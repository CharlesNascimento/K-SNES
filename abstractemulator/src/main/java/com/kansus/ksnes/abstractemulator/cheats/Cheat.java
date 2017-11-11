package com.kansus.ksnes.abstractemulator.cheats;

/**
 * Defines the basic data of an emulator's cheat.
 * <p>
 * Created by Charles Nascimento on 03/08/2017.
 */
public class Cheat {

    //region Variables

    private String mCode = "";
    private String mName = "";
    private boolean mEnabled = false;

    //endregion

    //region Initializers

    /**
     * Default constructor.
     *
     * @param name    The name of the cheat.
     * @param code    The code of the cheat.
     * @param enabled Whether the cheat is enabled.
     */
    public Cheat(String name, String code, boolean enabled) {
        this.mName = name;
        this.mCode = code;
        this.mEnabled = enabled;
    }

    //endregion

    //region Getters

    /**
     * Returns the cheat code.
     *
     * @return The cheat code.
     */
    public String getCode() {
        return mCode;
    }

    /**
     * Returns the cheat name.
     *
     * @return The cheat name.
     */
    public String getName() {
        return mName;
    }

    /**
     * Checks whether the cheat is enabled.
     *
     * @return Whether the cheat is enabled.
     */
    public boolean isEnabled() {
        return mEnabled;
    }

    //endregion

    //region Setters

    /**
     * Sets the name of the cheat.
     *
     * @param name The new name of the cheat.
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Sets whether the cheat is enabled.
     *
     * @param enabled Whether the cheat should be enabled.
     */
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    //endregion

    //region Overrides

    @Override
    public String toString() {
        if (mName == null)
            return mCode;
        return mName + "\n" + mCode;
    }

    //endregion
}
