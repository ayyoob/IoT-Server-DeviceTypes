package org.wso2.carbon.iot.android.sensebot;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.wso2.carbon.iot.android.sensebot.constants.SenseConstants;
import org.wso2.carbon.iot.android.sensebot.util.LocalRegistry;
import org.wso2.carbon.iot.android.sensebot.util.SenseClientAsyncExecutor;
import org.wso2.carbon.iot.android.sensebot.util.SensebotDriveAsyncExecutor;
import org.wso2.carbon.iot.android.sensebot.util.dto.AccessTokenInfo;
import org.wso2.carbon.iot.android.sensebot.util.dto.OAuthRequestInterceptor;
import org.wso2.carbon.iot.android.sensebot.util.dto.SensebotService;
import org.wso2.carbon.iot.android.sensebot.util.dto.TokenIssuerService;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import agent.sense.android.iot.carbon.wso2.org.wso2_sensebot.R;
import feign.Client;
import feign.Feign;
import feign.FeignException;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;

public class SensebotControl extends AppCompatActivity {

    Spinner deviceIds;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensebot_control);

        Button forward = (Button) findViewById(R.id.forward);
        setListener(forward, "forward");

        Button reverse = (Button) findViewById(R.id.reverse);
        setListener(reverse, "reverse");

        Button stop = (Button) findViewById(R.id.stopCar);
        setListener(stop, "stop");

        Button left = (Button) findViewById(R.id.left);
        setListener(left, "left");

        Button right = (Button) findViewById(R.id.right);
        setListener(right, "right");

        Button logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                LocalRegistry.removeTenantDomain(context);
                LocalRegistry.removeRefreshToken(context);
                LocalRegistry.removeAccessToken(context);
                LocalRegistry.removeClientId(context);
                LocalRegistry.removeClientSecret(context);
                LocalRegistry.removeDeviceId(context);
                LocalRegistry.removeUsername(context);
                LocalRegistry.removeServerURL(context);
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        deviceIds = (Spinner) findViewById(R.id.deviceIds);
        Set<String> devices = LocalRegistry.getDeviceId(getApplicationContext());
        String arraySpinner[] = devices.toArray(new String[devices.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        deviceIds.setAdapter(adapter);


    }

    private void setListener(Button button, final String direct) {
        button.setOnClickListener(new View.OnClickListener() {
            String direction = direct;
            @Override
            public void onClick(View view) {
                String deviceId = (String) deviceIds.getSelectedItem();
                SensebotDriveAsyncExecutor sensebotDriveAsyncExecutor = new SensebotDriveAsyncExecutor(getApplicationContext());
                sensebotDriveAsyncExecutor.execute(deviceId, direction);
            }
        });
    }

}
