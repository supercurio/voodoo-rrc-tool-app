
package org.projectvoodoo.rrc;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    static final String REQUIRE_REBOOT_DONT_KILL_RILD[] = {
            "SGH-I997.*",
            "SCH-I510.*"
    };

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
    }

}
