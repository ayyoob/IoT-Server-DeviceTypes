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

package org.wso2.carbon.iot.android.sensebot.util.dto;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * This holds the android manager service definition that is used with netflix feign.
 */
public interface SensebotService {

    @Path("/devices")
    @GET
    String[] getDevices(@HeaderParam("referer") String referer);

    @Path("device/{deviceId}/direction/{direction}")
    @POST
    void changeDirection(@HeaderParam("referer") String referer, @PathParam("deviceId") String deviceId,
                             @PathParam("direction") String direction);

}
