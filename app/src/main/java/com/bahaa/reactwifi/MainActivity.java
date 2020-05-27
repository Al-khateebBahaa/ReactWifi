package com.bahaa.reactwifi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText mNetworkOne;
    private EditText mPasswordOne;

    private String mBaseNetwork;
    private String mBaseNetworkPassword;

    private WifiManager wifiManager;
    private NetworkUtils networkUtils;
    int flag = 0;


    private static final String TAG = "MainActivity";

    public static String[] PERMISSIONS_REQUIRED = new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_NETWORK_STATE", "android.permission.ACCESS_WIFI_STATE", "android.permission.CHANGE_WIFI_STATE"}; //Manifest.permission.PERMISSION_NAME

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        networkUtils = NetworkUtils.getsInstance();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
            hasPermissionToDo(this, PERMISSIONS_REQUIRED);


    }


    private void bindViews() {

        mNetworkOne = findViewById(R.id.ssid1_edit_text);
        mPasswordOne = findViewById(R.id.password1_edit_text);


    }

    private boolean validateViews() {
        if (mNetworkOne.getText().toString().isEmpty()) {
            mNetworkOne.setError("This field is Empty");
            return false;
        } else if (mPasswordOne.getText().toString().isEmpty()) {
            mNetworkOne.setError("This field is Empty");
            return false;
        }
        mBaseNetwork = mNetworkOne.getText().toString();
        mBaseNetworkPassword = mPasswordOne.getText().toString();

        return true;

    }


    public void connect(View view) {


        //if (validateViews()) {
        //   if (scanForNetworks())
        if (reconnectWIFI("wep2", mBaseNetwork, mBaseNetworkPassword)) {
            Toast.makeText(this, "connecting with " + mBaseNetwork, Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (wifiManager.isWifiEnabled())
                        createRequest();
                    else
                        reconnectWIFI("wep2", mBaseNetwork, mBaseNetworkPassword);
                }
            }, 20000);
            //    } else {
        }
        //   }

    }


    public boolean reconnectWIFI(String networkType, String networkSSID, String networkPass) {

        wifiManager.setWifiEnabled(true);

        if (networkType.equals("open")) {
            try {
                WifiConfiguration conf = new WifiConfiguration();
                conf.SSID = "\"" + networkSSID + "\"";
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiManager.addNetwork(conf);
                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                for (WifiConfiguration i : list) {
                    if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(i.networkId, true);
                        wifiManager.reconnect();
                        break;
                    }
                }
                Toast.makeText(this, "wifi connected", Toast.LENGTH_SHORT).show();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "reconnectWIFI: status is fail");
                return false;
            }
        } else {
            try {
                WifiConfiguration conf = new WifiConfiguration();
                conf.SSID = "\"" + networkSSID + "\"";
                conf.preSharedKey = "\"" + networkPass + "\"";
                conf.status = WifiConfiguration.Status.ENABLED;
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiManager.addNetwork(conf);
                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                for (WifiConfiguration i : list) {
                    if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(i.networkId, true);
                        wifiManager.reconnect();
                        break;
                    }
                }
                Toast.makeText(this, "wifi connected", Toast.LENGTH_SHORT).show();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "reconnectWIFI: status is fail");
                return false;
            }
        }
    }


    private void createRequest() {

        ((TextView) findViewById(R.id.status)).setText("status: http://192.168.4.1/wi?s1=FiberHGW_TP839C_2.4GHz&p1=Pjq4vbyP&save=");

        networkUtils.getNetworkRequests().getData().enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(@NotNull Call<ResponseData> call, @NotNull Response<ResponseData> response) {


                if (response.body() != null) {
                    Toast.makeText(MainActivity.this, "تم جلب البيانات بنجاح", Toast.LENGTH_LONG).show();
                    ((TextView) findViewById(R.id.status)).setText(response.body().toString());
                    ((TextView) findViewById(R.id.status)).setText(response.body().getName());
                    wifiManager.disconnect();
                } else {
                    try {
                        Toast.makeText(MainActivity.this, "حدث خطأ" + response.errorBody().toString(), Toast.LENGTH_LONG).show();
                    } catch (NullPointerException e) {
                        ((TextView) findViewById(R.id.status)).setText(response.errorBody().toString());

                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseData> call, @NotNull Throwable t) {
                //    Toast.makeText(MainActivity.this, "حدث خط أثناء الإتصال بالخادم", Toast.LENGTH_LONG).show();
                ((TextView) findViewById(R.id.status)).setText(t.getMessage());
                Log.e(TAG, "onFailure: fail is > " + t.getMessage());
            }
        });
    }

    private boolean hasPermissionToDo(final Activity context, final String[] permissions) {
        boolean oneDenied = false;
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ContextCompat.checkSelfPermission(context, permission)
                            != PackageManager.PERMISSION_GRANTED)
                oneDenied = true;
        }

        if (!oneDenied) return true;

        boolean showRationale = MainActivity.this.shouldShowRequestPermissionRationale("android.permission.ACCESS_FINE_LOCATION");

        if (flag == 0) {
            showRationale = !showRationale;
            flag++;
            Log.e("ok", flag + "");
        }

        if (!showRationale) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Location permission is required to use this app. Please grant the permission from CasinoFi app settings.");
            builder.setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                    startActivity(intent);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.common_permission_explaination);
            builder.setPositiveButton(R.string.common_permission_grant, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Fire off an async request to actually get the permission
                    // This will show the standard permission request dialog UI
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        context.requestPermissions(permissions, 1);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        return false;
    }


}



