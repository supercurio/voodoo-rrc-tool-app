
package org.projectvoodoo.rrc;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    static final String VALID_BUILD_MODEL[] = {
            "GT-I9300"
    };

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
    }

}
