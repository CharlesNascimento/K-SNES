package com.kansus.ksnes.snes9x;

import android.util.Xml;

import com.kansus.ksnes.abstractemulator.CheatManager;
import com.kansus.ksnes.abstractemulator.Cheat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class S9xCheatManager implements CheatManager {

    public native boolean nativeAdd(String code);

    public native void nativeRemove(String code);

    private static final String XML_ENCODING = "UTF-8";
    private File file;
    private ArrayList<Cheat> cheats = new ArrayList<Cheat>();
    private boolean modified;

    public S9xCheatManager(String romFile) {
        String path = romFile;
        int dot = path.lastIndexOf('.');
        if (dot >= 0)
            path = path.substring(0, dot);
        path += ".cht";

        file = new File(path);
        load();
    }

    @Override
    public final List<Cheat> getAll() {
        return cheats;
    }

    @Override
    public void setModified() {
        modified = true;
    }

    @Override
    public Cheat add(String code, String name) {
        Cheat c = add(code, name, true);
        if (c != null)
            modified = true;
        return c;
    }

    private Cheat add(String code, String name, boolean enabled) {
        if (code == null || code.length() == 0)
            return null;

        if (enabled && !nativeAdd(code))
            return null;

        // normalize
        if ("".equals(name))
            name = null;

        Cheat c = new S9xCheat();
        c.enabled = enabled;
        c.code = code;
        c.name = name;
        cheats.add(c);
        return c;
    }

    @Override
    public void remove(int i) {
        Cheat c = cheats.get(i);
        if (c.enabled)
            nativeRemove(c.code);
        cheats.remove(i);

        modified = true;
    }

    @Override
    public void enable(int i, boolean enabled) {
        S9xCheat c = (S9xCheat) cheats.get(i);
        if (c.enabled == enabled)
            return;

        c.enabled = enabled;
        if (enabled)
            nativeAdd(c.code);
        else
            nativeRemove(c.code);

        modified = true;
    }

    private void load() {
        try {
            BufferedInputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(file));

                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(in, XML_ENCODING);

                int event = parser.getEventType();
                while (event != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG &&
                            parser.getName().equals("item")) {
                        String code = parser.getAttributeValue(null, "code");
                        String name = parser.getAttributeValue(null, "name");
                        boolean enabled = !"true".equals(
                                parser.getAttributeValue(null, "disabled"));
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

    @Override
    public void save() {
        if (!modified)
            return;

        // delete file if no cheats
        if (cheats.size() == 0 && file.delete())
            return;

        try {
            BufferedOutputStream out = null;
            try {
                out = new BufferedOutputStream(new FileOutputStream(file));

                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(out, XML_ENCODING);
                serializer.startDocument(null, null);
                serializer.startTag(null, "cheats");
                for (Cheat c : cheats) {
                    serializer.startTag(null, "item");

                    if (!c.enabled)
                        serializer.attribute(null, "disabled", "true");
                    serializer.attribute(null, "code", c.code);
                    if (c.name != null)
                        serializer.attribute(null, "name", c.name);

                    serializer.endTag(null, "item");
                }
                serializer.endTag(null, "cheats");
                serializer.endDocument();

            } finally {
                if (out != null)
                    out.close();
            }
        } catch (Exception e) {
        }

        modified = false;
    }

    @Override
    public void destroy() {
        save();

        for (int i = cheats.size(); --i >= 0; ) {
            Cheat c = cheats.get(i);
            if (c.enabled)
                nativeRemove(c.code);
        }
        cheats.clear();
    }
}
