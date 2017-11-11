package com.kansus.ksnes.snes9x;

import android.util.Log;
import android.util.Xml;

import com.kansus.ksnes.abstractemulator.cheats.Cheat;
import com.kansus.ksnes.abstractemulator.cheats.CheatsModule;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

// FIXME Remove all android dependencies

/**
 * Snes9x implementation of a module that handles cheats.
 * <p>
 * Created by Charles Nascimento on 29/07/2017.
 */
public class S9xCheatsModule implements CheatsModule {

    //region Constants

    private static final String XML_ENCODING = "UTF-8";

    //endregion

    //region Variables

    private boolean mIsEnabled = true;
    private File mCheatFile;
    private ArrayList<Cheat> mCheats = new ArrayList<>();
    private boolean modified;

    //endregion

    //region Initializers

    /**
     * Default constructor.
     */
    public S9xCheatsModule() {
    }

    /**
     * Constructs a new module with a predefined ROM.
     *
     * @param romFile The current ROM file name.
     */
    public S9xCheatsModule(String romFile) {
        setROM(romFile);
    }

    //endregion

    //region CheatsModule

    @Override
    public final List<Cheat> getAll() {
        return mCheats;
    }

    @Override
    public void setModified() {
        modified = true;
    }

    @Override
    public Cheat add(String code, String name) {
        Cheat c = add(code, name, true);

        if (c != null) {
            modified = true;
        }

        return c;
    }

    @Override
    public void remove(int i) {
        Cheat c = mCheats.get(i);

        if (c.isEnabled()) {
            nativeRemove(c.getCode());
        }

        mCheats.remove(i);
        modified = true;
    }

    @Override
    public void setCheatEnabled(int i, boolean enabled) {
        Cheat c = mCheats.get(i);
        if (c.isEnabled() == enabled)
            return;

        c.setEnabled(enabled);
        if (enabled)
            nativeAdd(c.getCode());
        else
            nativeRemove(c.getCode());

        modified = true;
    }

    @Override
    public void save() {
        if (!modified)
            return;

        // delete file if no cheats
        if (mCheats.size() == 0 && mCheatFile.delete())
            return;

        try {
            BufferedOutputStream out = null;
            try {
                out = new BufferedOutputStream(new FileOutputStream(mCheatFile));

                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(out, XML_ENCODING);
                serializer.startDocument(null, null);
                serializer.startTag(null, "cheats");
                for (Cheat c : mCheats) {
                    serializer.startTag(null, "item");

                    if (!c.isEnabled())
                        serializer.attribute(null, "disabled", "true");
                    serializer.attribute(null, "code", c.getCode());
                    if (c.getName() != null)
                        serializer.attribute(null, "name", c.getName());

                    serializer.endTag(null, "item");
                }
                serializer.endTag(null, "cheats");
                serializer.endDocument();

            } finally {
                if (out != null)
                    out.close();
            }
        } catch (Exception e) {
            Log.e("S9xCheatsModule", e.getMessage());
        }

        modified = false;
    }

    @Override
    public void enable() {
        if (mIsEnabled) {
            return;
        }

        if (mCheatFile != null) {
            load();
        }

        mIsEnabled = true;
    }

    @Override
    public void disable() {
        if (!mIsEnabled) {
            return;
        }

        save();

        for (int i = mCheats.size(); --i >= 0; ) {
            Cheat c = mCheats.get(i);

            if (c.isEnabled()) {
                nativeRemove(c.getCode());
            }
        }
        mCheats.clear();

        mIsEnabled = false;
    }

    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    @Override
    public void setROM(String rom) {
        if (rom == null) {
            disable();
            mCheatFile = null;
            return;
        }

        mCheatFile = getCheatFileFor(rom);

        if (mIsEnabled) {
            load();
        }
    }

    //endregion

    //region Private methods

    /**
     * Creates a cheats file name based on the ROM's file name.
     *
     * @param romFile The name of the ROM.
     * @return A cheats file.
     */
    private File getCheatFileFor(String romFile) {
        String path = romFile;
        int dot = path.lastIndexOf('.');

        if (dot >= 0) {
            path = path.substring(0, dot);
        }

        path += ".cht";

        return new File(path);
    }

    /**
     * Loads cheats from a persistent storage.
     */
    private void load() {
        try {
            BufferedInputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(mCheatFile));

                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(in, XML_ENCODING);

                int event = parser.getEventType();
                while (event != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG &&
                            parser.getName().equals("item")) {
                        String code = parser.getAttributeValue(null, "code");
                        String name = parser.getAttributeValue(null, "name");
                        boolean enabled = !"true".equals(parser.getAttributeValue(null, "disabled"));
                        add(code, name, enabled);
                    }
                    event = parser.next();
                }
            } finally {
                if (in != null)
                    in.close();
            }
        } catch (FileNotFoundException fne) {
            // don't report error if file simply doesn't exist
        } catch (Exception e) {
            e.printStackTrace();
        }

        modified = false;
    }

    /**
     * Tries to add a new cheat to the emulation engine. If successful,
     * the cheat is added to the list of cheats of this module.
     *
     * @param code    The code of the cheat.
     * @param name    The name of the cheat.
     * @param enabled Whether the cheat is enabled.
     * @return The newly created Cheat object.
     */
    private Cheat add(String code, String name, boolean enabled) {
        if (code == null || code.length() == 0)
            return null;

        if (enabled && !nativeAdd(code))
            return null;

        // normalize
        if ("".equals(name))
            name = null;

        Cheat c = new Cheat(name, code, enabled);
        mCheats.add(c);
        return c;
    }

    //endregion

    //region Native

    /**
     * Adds a cheat code to the emulation engine.
     *
     * @param code The code of the cheat.
     * @return Whether the code was successfully added.
     */
    private native boolean nativeAdd(String code);

    /**
     * Removes a cheat code from the emulation engine.
     *
     * @param code The code of the cheat.
     */
    private native void nativeRemove(String code);

    //endregion
}
