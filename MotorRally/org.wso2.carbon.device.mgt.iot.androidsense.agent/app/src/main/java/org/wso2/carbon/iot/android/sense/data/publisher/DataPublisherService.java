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
package org.wso2.carbon.iot.android.sense.data.publisher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.iot.android.sense.RegisterActivity;
import org.wso2.carbon.iot.android.sense.data.publisher.mqtt.AndroidSenseMQTTHandler;
import org.wso2.carbon.iot.android.sense.data.publisher.mqtt.transport.MQTTTransportHandler;
import org.wso2.carbon.iot.android.sense.data.publisher.mqtt.transport.TransportHandlerException;
import org.wso2.carbon.iot.android.sense.constants.SenseConstants;
import org.wso2.carbon.iot.android.sense.event.streams.Location.LocationData;
import org.wso2.carbon.iot.android.sense.util.SenseDataHolder;
import org.wso2.carbon.iot.android.sense.util.LocalRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an android service which publishes the data to the server.
 */
public class DataPublisherService extends Service {
    private static final String TAG = DataPublisherService.class.getName();
    private static String KEY_TAG = "key";
    private static String TIME_TAG = "time";
    private static String VALUE_TAG = "value";
    public static Context context;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        if (!LocalRegistry.isExist(getApplicationContext())) {
            return Service.START_NOT_STICKY;
        }

        Log.d(TAG, "service started");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    List<Event> events = new ArrayList<>();
                    List<LocationData> locationDataForDB = new ArrayList<>();
                    //retrieve location data.
                    List<LocationData> locationDataMap = SenseDataHolder.getLocationDataHolder();

                    if (!locationDataMap.isEmpty()) {
                        for (LocationData locationData : locationDataMap) {
                            Event event = new Event();
                            event.setTimestamp(locationData.getTimeStamp());
                            event.setGps(new double[]{locationData.getLatitude(), locationData.getLongitude()});
                            events.add(event);

                            LocationData data = new LocationData();
                            data.setLatitude(locationData.getLatitude());
                            data.setLongitude(locationData.getLongitude());
                            data.setTimeStamp(locationData.getTimeStamp());
                            locationDataForDB.add(locationData);
                        }
                     }
                    SenseDataHolder.resetLocationDataHolder();

                    if (locationDataForDB.size() > 0) {
                        DBWriter dbW=new DBWriter(context);
                        dbW.insertData(locationDataForDB);
                    }
                    //publish the data
                    if (events.size() > 0 && LocalRegistry.isEnrolled(context)) {
                        String user = LocalRegistry.getUsername(context);
                        String deviceId = LocalRegistry.getDeviceId(context);
                        JSONArray jsonArray = new JSONArray();
                        for (Event event : events) {
                            event.setOwner(user);
                            event.setDeviceId(deviceId);
                            jsonArray.put(new JSONObject().put("event", event.getEvent()));
                        }

                        MQTTTransportHandler mqttTransportHandler = AndroidSenseMQTTHandler.getInstance(context);
                        if (!mqttTransportHandler.isConnected()) {
                            mqttTransportHandler.connect();
                        }
                        String topic = LocalRegistry.getTenantDomain(context) + "/" + SenseConstants.DEVICE_TYPE + "/" + deviceId + "/data";
                        mqttTransportHandler.publishDeviceData(user, deviceId, jsonArray.toString(), topic);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Json Data Parsing Exception", e);
                } catch (TransportHandlerException e) {
                    Log.e(TAG, "Data Publish Failed", e);
                }
            }
        };
        Thread dataUploaderThread = new Thread(runnable);
        dataUploaderThread.start();
        return Service.START_NOT_STICKY;
    }
}