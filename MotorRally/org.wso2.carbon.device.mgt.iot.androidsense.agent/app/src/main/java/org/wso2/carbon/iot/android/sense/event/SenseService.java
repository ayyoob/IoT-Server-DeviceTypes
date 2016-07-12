/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.wso2.carbon.iot.android.sense.event;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.wso2.carbon.iot.android.sense.event.streams.Location.SenseLocationListener;
import org.wso2.carbon.iot.android.sense.util.LocalRegistry;
import org.wso2.carbon.iot.android.sense.util.SenseWakeLock;

/**
 * This service caters to initiate the data collection.
 */
public class SenseService extends Service {

    public static Context context;
    private static LocationListener listener;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 3; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        if (!LocalRegistry.isExist(context)) return Service.START_NOT_STICKY;
        configLocationListener();
        //service will not be stopped until we manually stop the service
        return Service.START_STICKY;

    }

    public void configLocationListener() {
        listener = new SenseLocationListener();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // getting network status
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
        } else {
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
            //        MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SenseWakeLock.releaseCPUWakeLock();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}