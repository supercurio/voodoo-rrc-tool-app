
package org.projectvoodoo.rrc.samsung;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.projectvoodoo.rrc.App;
import org.projectvoodoo.rrc.Utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class NwkInfo {

    private static final String TAG = "Voodoo RRC Tool NwkInfo";

    public static final String TABLE_NAME = "dormpolicy";
    public static final String NETWORK_ID = "plmn";
    public static final String NETWORK_NAME = "nwkname";
    public static final String LCD_ON_FD_TIME = "lcdonfdtime";
    public static final String LCD_OFF_FD_TIME = "lcdofffdtime";

    public static final String DB_FILENAME = "nwk_info.db";

    public static final String DB_DBDATA_FULLPATH =
            "/dbdata/databases/com.android.providers.telephony/" + DB_FILENAME;
    public static final String DB_DATA_FULLPATH =
            "/data/data/com.android.providers.telephony/databases/" + DB_FILENAME;

    private String mDatabaseFullPath;

    private Integer mNetworkId;
    private String mNetworkName;
    // set default Fd Time to 20s
    private Integer mLcdOnFdTime = 20;
    private Integer mLcdOffFdTime = 0;

    private boolean mInDb;
    private boolean mFdEnabled = true;

    public NwkInfo(int networkId, String networkName) throws IOException {
        cacheTelephonyProvidersDbFile();

        SQLiteDatabase db = openDb(false);

        Cursor cursor = db.query(TABLE_NAME,
                new String[] {
                        NETWORK_NAME,
                        LCD_ON_FD_TIME,
                        LCD_OFF_FD_TIME,
                },
                NETWORK_ID + " = ?",
                new String[] {
                    networkId + "",
                }, null, null, null);

        mNetworkId = networkId;

        if (cursor.getCount() == 1) {

            cursor.moveToFirst();
            mNetworkName = cursor.getString(0);
            mLcdOnFdTime = cursor.getInt(1);
            mLcdOffFdTime = cursor.getInt(2);
            setFdEnabledFromTimes();

            Log.i(TAG, "Network " + mNetworkId + " (" + networkName + ") in database");
            mInDb = true;
        } else {
            Log.i(TAG, "Network " + mNetworkId + " (" + networkName + ") not in the database");
            mNetworkName = networkName;
        }

        cursor.close();
        db.close();
    }

    public boolean isInDb() {
        return mInDb;
    }

    public int getNetworkId() {
        return mNetworkId;
    }

    public String getNetworkName() {
        return mNetworkName;
    }

    public int getFdTime() {
        return mLcdOnFdTime;
    }

    public void setTimes(int lcdOn, int lcdOff) {
        mLcdOnFdTime = lcdOn;
        mLcdOffFdTime = lcdOff;
    }

    public boolean isFdEnabled() {
        return mFdEnabled;
    }

    public void setFdEnabled(boolean enabled) {
        mFdEnabled = enabled;
    }

    public void writeCachedDb() {
        SQLiteDatabase db = openDb(true);
        ContentValues values = new ContentValues();
        values.put(LCD_ON_FD_TIME, mFdEnabled ? mLcdOnFdTime : 0);
        values.put(LCD_OFF_FD_TIME, mFdEnabled ? mLcdOffFdTime : 0);

        String message = "network " + mNetworkId + " (" + mNetworkName + ") in database to "
                + values.getAsString(LCD_ON_FD_TIME) + "/" + values.getAsString(LCD_OFF_FD_TIME);

        if (!mInDb) {
            Log.i(TAG, "Add network " + message);

            values.put(NETWORK_NAME, mNetworkName);
            values.put(NETWORK_ID, mNetworkId);

            db.insert(TABLE_NAME, null, values);
            mInDb = true;
        } else {
            Log.i(TAG, "Update network " + message);

            db.update(TABLE_NAME, values, NETWORK_ID + " = ?", new String[] {
                    mNetworkId + ""
            });
        }

        db.close();
    }

    public void apply() throws IOException {
        writeCachedDb();
        copyFileAsRoot(App.context.getCacheDir() + "/" + DB_FILENAME, mDatabaseFullPath);
        Utils.killRild();
    }

    private void cacheTelephonyProvidersDbFile() throws IOException {

        if (new File(DB_DBDATA_FULLPATH).exists())
            mDatabaseFullPath = DB_DBDATA_FULLPATH;
        else {
            ArrayList<String> out = Utils.run("su", "ls " + DB_DATA_FULLPATH);
            if (out.size() == 1 && out.get(0).equals(DB_DATA_FULLPATH))
                mDatabaseFullPath = DB_DATA_FULLPATH;
        }

        if (mDatabaseFullPath != null) {
            Log.i(TAG, DB_FILENAME + " database found: " + mDatabaseFullPath);

            copyFileAsRoot(mDatabaseFullPath, App.context.getCacheDir() + "/" + DB_FILENAME);
        }
    }

    private void copyFileAsRoot(String source, String destination) throws IOException {
        Utils.run("su", "cat " + source + " > " + destination);
    }

    private void setFdEnabledFromTimes() {
        if (mLcdOnFdTime > 0 || mLcdOffFdTime > 0)
            mFdEnabled = true;
        else
            mFdEnabled = false;
    }

    private SQLiteDatabase openDb(boolean write) {
        int flag = write ? SQLiteDatabase.OPEN_READWRITE : SQLiteDatabase.OPEN_READONLY;

        return SQLiteDatabase.openDatabase(
                App.context.getCacheDir() + "/" + DB_FILENAME,
                null,
                flag);
    }

}
