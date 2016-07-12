package org.wso2.carbon.iot.android.sense;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.wso2.carbon.iot.android.sense.data.publisher.DataPublisherService;
import org.wso2.carbon.iot.android.sense.event.SenseService;
import org.wso2.carbon.iot.android.sense.util.LocalRegistry;

import agent.sense.android.iot.carbon.wso2.org.wso2_senseagent.R;

public class LogOut extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_out);
        if (!LocalRegistry.isExist(getApplicationContext())) {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
            finish();
        }
        if (!isMyServiceRunning(SenseService.class)) {
            Intent locationIntent = new Intent(getApplicationContext(), SenseService.class);
            startService(locationIntent);
        }

        Button deviceRegisterButton = (Button) findViewById(R.id.logout);
        deviceRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!LocalRegistry.isExist(getApplicationContext())) {
                    Intent activity = new Intent(getApplicationContext(), RegisterActivity.class);
                    startActivity(activity);
                }
                LocalRegistry.setEnrolled(getApplicationContext(), false);
                LocalRegistry.removeUsername(getApplicationContext());
                LocalRegistry.removeDeviceId(getApplicationContext());
                LocalRegistry.removeServerURL(getApplicationContext());
                LocalRegistry.removeAccessToken(getApplicationContext());
                LocalRegistry.removeRefreshToken(getApplicationContext());
                LocalRegistry.removeMqttEndpoint(getApplicationContext());
                LocalRegistry.removeTenantDomain(getApplicationContext());
                LocalRegistry.setExist(false);

                Intent registerActivity = new Intent(getApplicationContext(), RegisterActivity.class);
                registerActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(registerActivity);
                finish();
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
