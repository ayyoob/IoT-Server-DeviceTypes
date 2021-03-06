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
package org.wso2.carbon.iot.android.sensebot.constants;

/**
 * This hold constants related to android_sense.
 */
public class SenseConstants {
    public final static String DEVICE_TYPE = "sensebot";
    public final static String SENSEBOT_CONTEXT = "/sensebot";
    public final static String DCR_CONTEXT = "/dynamic-client-web";
    public final static String TOKEN_ISSUER_CONTEXT = "/token";
    public final static String DEFAULT_SCOPE = "sensebot_user";
    public final static String API_APPLICATION_REGISTRATION_CONTEXT = "/api-application-registration";

    public final class Request {
        public final static String REQUEST_SUCCESSFUL = "200";
        public final static int MAX_ATTEMPTS = 2;
    }
}
