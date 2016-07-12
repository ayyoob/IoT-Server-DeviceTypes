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
package org.wso2.carbon.iot.android.sense.util;

import org.wso2.carbon.iot.android.sense.event.streams.Location.LocationData;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import android.util.Log;


/**
 * This holds the sensor,battery and location data inmemory.
 */
public class SenseDataHolder {

    private static List<LocationData> locationDataHolder;
    private static final String TAG = SenseDataHolder.class.getName();

    public static List<LocationData> getLocationDataHolder(){
        if(locationDataHolder == null){
            locationDataHolder = new CopyOnWriteArrayList<>();
        }
            return locationDataHolder;

    }

    public static void resetLocationDataHolder(){
        locationDataHolder = null;
    }

}
