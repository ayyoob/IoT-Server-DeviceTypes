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
package org.wso2.carbon.iot.android.sensebot.util;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.wso2.carbon.iot.android.sensebot.constants.SenseConstants;
import org.wso2.carbon.iot.android.sensebot.util.dto.AccessTokenInfo;
import org.wso2.carbon.iot.android.sensebot.util.dto.OAuthRequestInterceptor;
import org.wso2.carbon.iot.android.sensebot.util.dto.SensebotService;
import org.wso2.carbon.iot.android.sensebot.util.dto.TokenIssuerService;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import feign.Client;
import feign.Feign;
import feign.FeignException;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;

public class SensebotDriveAsyncExecutor extends AsyncTask<String, Void, Map<String, String>> {

    private final static String TAG = "SenseService Client";
    private static final String STATUS = "status";
    private final static String DEVICE_NAME = Build.MANUFACTURER + " " + Build.MODEL;
    private Context context;

    public SensebotDriveAsyncExecutor(Context context) {
        this.context = context;

    }

    TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
    };

    Client disableHostnameVerification = new Client.Default(getTrustedSSLSocketFactory(), new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
    });

    @Override
    protected Map<String, String> doInBackground(String... parameters) {
        if (android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        Map<String, String> response = new HashMap<>();
        String deviceId = parameters[0];
        String direction = parameters[1];
        String endpoint = LocalRegistry.getServerURL(context);
        String accessToken = LocalRegistry.getAccessToken(context);
        if (deviceId != null && !deviceId.isEmpty()) {
            try {
                SensebotService sensebotService = Feign.builder().client(disableHostnameVerification)
                        .requestInterceptor(new OAuthRequestInterceptor(accessToken))
                        .contract(new JAXRSContract()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                        .target(SensebotService.class, endpoint + SenseConstants.SENSEBOT_CONTEXT);

                sensebotService.changeDirection("carbon.super", deviceId, direction);
            } catch (FeignException e) {
                if (e.status() == 401) {
                    TokenIssuerService tokenIssuerService = Feign.builder().client(disableHostnameVerification).requestInterceptor(
                            new BasicAuthRequestInterceptor(LocalRegistry.getClientId(context), LocalRegistry.getClientSecret
                                    (context)))
                            .contract(new JAXRSContract()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                            .target(TokenIssuerService.class, endpoint + SenseConstants.TOKEN_ISSUER_CONTEXT);
                    AccessTokenInfo accessTokenInfo = tokenIssuerService.getRefreshToken("refresh_token", LocalRegistry
                            .getRefreshToken(context), SenseConstants.DEFAULT_SCOPE);
                    accessToken = accessTokenInfo.getAccess_token();
                    LocalRegistry.addAccessToken(context, accessToken);
                    LocalRegistry.addRefreshToken(context, accessTokenInfo.getRefresh_token());

                    SensebotService sensebotService = Feign.builder().client(disableHostnameVerification)
                            .requestInterceptor(new OAuthRequestInterceptor(accessToken))
                            .contract(new JAXRSContract()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                            .target(SensebotService.class, endpoint + SenseConstants.SENSEBOT_CONTEXT);

                    sensebotService.changeDirection("carbon.super", deviceId, direction);

                }
            }
        }
        return response;

    }

    private SSLSocketFactory getTrustedSSLSocketFactory() {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return sc.getSocketFactory();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            Log.e(SensebotDriveAsyncExecutor.class.getName(), "Invalid Certificate");
            return null;
        }
    }
}
