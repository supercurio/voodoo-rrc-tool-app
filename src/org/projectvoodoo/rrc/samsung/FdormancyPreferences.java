
package org.projectvoodoo.rrc.samsung;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.projectvoodoo.rrc.App;

import android.content.SharedPreferences;
import android.util.Log;

public class FdormancyPreferences {

    private static final String TAG = "Voodoo RRC Tool FdormancyPreferences";

    private static final String PREF_FILENAME = "fdormancy.preferences_name";
    private static final String PREF_EXT = ".xml";

    private static final String KEY_STATE = "fdormancy.key.state";
    private static final String KEY_MMCMNC = "fdormancy.key.mccmnc";

    private static final String[] SYSTEM_PREFS_PATHS = {
            "/data/data/com.android.phone/shared_prefs",
            "/dbdata/databases/com.android.phone/shared_prefs",
    };

    private final String mPrefsFullPath;

    public FdormancyPreferences() throws IOException {

        String prefsFullPath = null;

        for (String path : SYSTEM_PREFS_PATHS) {
            String fullPath = path + "/" + PREF_FILENAME + PREF_EXT;

            if (new File(fullPath).exists()) {
                prefsFullPath = fullPath;
                Log.i(TAG, "fdormancy preferences found: " + fullPath);
                break;
            }
        }

        if (prefsFullPath == null)
            throw new IOException();
        else
            mPrefsFullPath = prefsFullPath;

        App.context.getSharedPreferences(PREF_FILENAME, 0).edit().commit();
        copyPrefsToLocal();
    }

    public int getNetworkId() {
        try {
            return Integer.parseInt(
                    App.context.getSharedPreferences(PREF_FILENAME, 0)
                            .getString(KEY_MMCMNC, "0"));
        } catch (Exception e) {
            return 0;
        }
    }

    public void setEnabled(int networkId, boolean enabled) {
        Log.i(TAG, "Set enabled: " + enabled);

        App.context.getSharedPreferences(PREF_FILENAME, 0)
                .edit()
                .putBoolean(KEY_STATE, enabled)
                .putString(KEY_MMCMNC, networkId + "")
                .commit();
    }

    public boolean isEnabled(Integer mNetworkId) {
        SharedPreferences prefs = App.context.getSharedPreferences(PREF_FILENAME, 0);

        return (prefs.getString(KEY_MMCMNC, "0").equals(mNetworkId + "")
        && prefs.getBoolean(KEY_STATE, true));
    }

    public void write() {
        writePrefs();
    }

    private void copyPrefsToLocal() {
        try {
            FileInputStream inputStream = new FileInputStream(mPrefsFullPath);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);

            FileOutputStream outputStream = new FileOutputStream(getLocalPrefsFileName());
            outputStream.write(buffer);
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writePrefs() {
        String outFileName = getLocalPrefsFileName();
        Log.v(TAG, "Write " + outFileName);

        try {
            FileInputStream inputStream = new FileInputStream(outFileName);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);

            FileOutputStream outputStream = new FileOutputStream(mPrefsFullPath);
            outputStream.write(buffer);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getLocalPrefsFileName() {
        String fileName = null;
        String packageName = App.context.getPackageName();

        String[] paths = {
                "/dbdata/databases",
                "/data/data"
        };

        for (String basePath : paths) {
            String fullPath = basePath + "/" + packageName
                    + "/shared_prefs/" + PREF_FILENAME + PREF_EXT;
            if (new File(fullPath).exists()) {
                fileName = fullPath;
                break;
            }
        }

        return fileName;
    }

}
