/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.devicetype.sensebot.plugin.constants;

import org.wso2.carbon.utils.CarbonUtils;
import java.io.File;

/**
 * Device type specific constants which includes all transport protocols configurations,
 * stream definition and device specific dome constants
 */
public class DeviceTypeConstants {
    public final static String DEVICE_TYPE = "sensebot";
    public final static String DEVICE_PLUGIN_DEVICE_NAME = "DEVICE_NAME";
    public final static String DEVICE_PLUGIN_DEVICE_ID = "sensebot_DEVICE_ID";
    public final static String MQTT_ADAPTER_TOPIC_PROPERTY_NAME = "mqtt.adapter.topic";
    public final static String FORWARD = "forward";
    public final static String REVERSE = "reverse";
    public final static String LEFT = "left";
    public final static String RIGHT = "right";
    public final static String STOP = "stop";

    //sensor events summerized table name
    public static final String SENSOR_TYPE1_EVENT_TABLE = "DEVICE_TEMPERATURE_SUMMARY";
    public static final String SENSOR_TYPE2_EVENT_TABLE = "DEVICE_SONAR_SUMMARY";
    public final static String DEVICE_TYPE_PROVIDER_DOMAIN = "carbon.super";
    public final static String SENSOR_TYPE1 = "temperature";
    public final static String SENSOR_TYPE2 = "sonar";

    //mqtt tranport related constants
    public static final String MQTT_ADAPTER_NAME = "temperature_mqtt";
    public static final String ADAPTER_TOPIC_PROPERTY = "topic";
    public static final String MQTT_PORT = "\\{mqtt.broker.port\\}";
    public static final String MQTT_BROKER_HOST = "\\{mqtt.broker.host\\}";
    public static final String CARBON_CONFIG_PORT_OFFSET = "Ports.Offset";
    public static final String DEFAULT_CARBON_LOCAL_IP_PROPERTY = "carbon.local.ip";
    public static final int CARBON_DEFAULT_PORT_OFFSET = 0;
    public static final int DEFAULT_MQTT_PORT = 1883;

    public final static String SENSOR_TYPE1_STREAM_DEFINITION = "org.wso2.iot.devices.temperature";
    public final static String SENSOR_TYPE1_STREAM_DEFINITION_VERSION = "1.0.0";

}

