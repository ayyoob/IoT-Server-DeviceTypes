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
import org.wso2.carbon.iot.android.sensebot.util.dto.SensebotService;
import org.wso2.carbon.iot.android.sensebot.util.dto.ApiApplicationRegistrationService;
import org.wso2.carbon.iot.android.sensebot.util.dto.ApiRegistrationProfile;
import org.wso2.carbon.iot.android.sensebot.util.dto.DynamicClientRegistrationService;
import org.wso2.carbon.iot.android.sensebot.util.dto.OAuthApplicationInfo;
import org.wso2.carbon.iot.android.sensebot.util.dto.OAuthRequestInterceptor;
import org.wso2.carbon.iot.android.sensebot.util.dto.RegistrationProfile;
import org.wso2.carbon.iot.android.sensebot.util.dto.TokenIssuerService;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

public class SenseClientAsyncExecutor extends AsyncTask<String, Void, Map<String, String>> {

    private final static String TAG = "SenseService Client";
    private static final String STATUS = "status";
    private final static String DEVICE_NAME = Build.MANUFACTURER + " " + Build.MODEL;
    private Context context;

    public SenseClientAsyncExecutor(Context context) {
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
        String response;
        Map<String, String> response_params = new HashMap<>();
        String username = parameters[0];
        String password = parameters[1];
        String endpoint = parameters[2];
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put(STATUS, "200");
        AccessTokenInfo accessTokenInfo = null;
        try {
            //DynamicClientRegistraiton.
            DynamicClientRegistrationService dynamicClientRegistrationService = Feign.builder()
                    .client(disableHostnameVerification).contract(new
                    JAXRSContract()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                    .target(DynamicClientRegistrationService.class, endpoint + SenseConstants.DCR_CONTEXT);
            RegistrationProfile registrationProfile = new RegistrationProfile();
            String applicationName = "android-sensebot";
            registrationProfile.setOwner(username);
            registrationProfile.setClientName(applicationName);
            registrationProfile.setCallbackUrl("");
            registrationProfile.setGrantType("password refresh_token client_credentials");
            registrationProfile.setApplicationType("device");
            registrationProfile.setTokenScope("production");
            OAuthApplicationInfo oAuthApplicationInfo = dynamicClientRegistrationService.register(registrationProfile);

            //PasswordGrantType
            TokenIssuerService tokenIssuerService = Feign.builder().client(disableHostnameVerification).requestInterceptor(
                    new BasicAuthRequestInterceptor(oAuthApplicationInfo.getClient_id(), oAuthApplicationInfo.getClient_secret()))
                    .contract(new JAXRSContract()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                    .target(TokenIssuerService.class, endpoint + SenseConstants.TOKEN_ISSUER_CONTEXT);
            accessTokenInfo = tokenIssuerService.getToken("password", username, password, "default");

            //ApiApplicationRegistration
            ApiApplicationRegistrationService apiApplicationRegistrationService = Feign.builder().client(disableHostnameVerification)
                    .requestInterceptor(new OAuthRequestInterceptor(accessTokenInfo.getAccess_token()))
                    .contract(new JAXRSContract()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                    .target(ApiApplicationRegistrationService.class, endpoint + SenseConstants.API_APPLICATION_REGISTRATION_CONTEXT);
            ApiRegistrationProfile apiRegistrationProfile = new ApiRegistrationProfile();
            apiRegistrationProfile.setApplicationName(applicationName);
            apiRegistrationProfile.setConsumerKey(oAuthApplicationInfo.getClient_id());
            apiRegistrationProfile.setConsumerSecret(oAuthApplicationInfo.getClient_secret());
            apiRegistrationProfile.setIsAllowedToAllDomains(false);
            apiRegistrationProfile.setIsMappingAnExistingOAuthApp(true);
            apiRegistrationProfile.setTags(new String[]{SenseConstants.DEVICE_TYPE});
            String replyMsg = apiApplicationRegistrationService.register(apiRegistrationProfile);
            accessTokenInfo = tokenIssuerService.getToken("password", username, password, SenseConstants.DEFAULT_SCOPE);

            SensebotService sensebotService = Feign.builder().client(disableHostnameVerification)
                    .requestInterceptor(new OAuthRequestInterceptor(accessTokenInfo.getAccess_token()))
                    .contract(new JAXRSContract()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                    .target(SensebotService.class, endpoint + SenseConstants.SENSEBOT_CONTEXT);

            String[] devices = sensebotService.getDevices("carbon.super");

            if (accessTokenInfo != null) {
                LocalRegistry.addAccessToken(context, accessTokenInfo.getAccess_token());
                LocalRegistry.addRefreshToken(context, accessTokenInfo.getRefresh_token());
                Set<String> deviceIds = new HashSet<String>(Arrays.asList(devices));
                LocalRegistry.addDeviceId(context, deviceIds);
            }
            return responseMap;
        } catch (FeignException e) {
            responseMap.put(STATUS, "" + e.status());
            return responseMap;
        }
    }

    private SSLSocketFactory getTrustedSSLSocketFactory() {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return sc.getSocketFactory();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            Log.e(SenseClientAsyncExecutor.class.getName(), "Invalid Certificate");
            return null;
        }
    }
}
