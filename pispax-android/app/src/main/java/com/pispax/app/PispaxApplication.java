package com.pispax.app;

import android.app.Application;
import org.osmdroid.config.Configuration;

public class PispaxApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // OSMDroid requiere que el user-agent se configure al arrancar la app
        Configuration.getInstance().setUserAgentValue(getPackageName());
    }
}
