package com.icebreaker.gizwits;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizEventType;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */

public class App extends Application {
    private static String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();

        //GizWifiSDK.sharedInstance().setLogLevel(GizLogPrintLevel.GizLogPrintNone);
        GizWifiSDK.sharedInstance().setListener(listener);
        GizWifiSDK.sharedInstance().startWithAppID(getApplicationContext(), "5bf80bcc85304bce8123ba584ee22d1d");
        //GizWifiSDK.sharedInstance().startWithAppID(this, "5bf80bcc85304bce8123ba584ee22d1d", null, null, null, false);
    }

    GizWifiSDKListener listener = new GizWifiSDKListener() {
        @Override
        public void didNotifyEvent(GizEventType eventType, Object eventSource, GizWifiErrorCode eventID, String eventMessage) {
            if (eventType == GizEventType.GizEventSDK) {
                // SDK的事件通知
                Log.i(TAG, "SDK event happened: " + eventID + ", " + eventMessage);

                GizWifiSDK.sharedInstance().userLoginAnonymous();

            } else if (eventType == GizEventType.GizEventDevice) {
                // 设备连接断开时可能产生的通知
                GizWifiDevice mDevice = (GizWifiDevice) eventSource;
                Log.i(TAG, "device mac: " + mDevice.getMacAddress() + " disconnect caused by eventID: " + eventID + ", eventMessage: " + eventMessage);
            } else if (eventType == GizEventType.GizEventM2MService) {
                // M2M服务返回的异常通知
                Log.i(TAG, "M2M domain " + (String) eventSource + " exception happened, eventID: " + eventID + ", eventMessage: " + eventMessage);
            } else if (eventType == GizEventType.GizEventToken) {
                // token失效通知
                Log.i(TAG, "token " + (String) eventSource + " expired: " + eventMessage);
            }
        }

        @Override
        public void didUserLogin(GizWifiErrorCode result, String uid, String token) {
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 登录成功
                Log.i(TAG, "Login success");

                List<GizWifiDevice> list = GizWifiSDK.sharedInstance().getDeviceList();
                Log.i(TAG, "Device list1 "+list.size());

                //GizWifiSDK.sharedInstance().bindRemoteDevice (uid, token, "virtual:site", "8ff25214c1c44286a8945ac4e65e7773", "2f7b79d25cc44a82a3b67f6903c2884a");

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        List<GizWifiDevice> list = GizWifiSDK.sharedInstance().getDeviceList();
                        Log.i(TAG, "Device list2 "+list.size());

                        if(list.size()>0){
                            GizWifiDevice device = list.get(0);
                            device.setListener(deviceListener);

                            device.setSubscribe(true);
                            //device.getHardwareInfo();

                        }else{
                            Log.i(TAG, "try");
                            new Handler(Looper.getMainLooper()).postDelayed(this,2000);
                        }
                    }
                },2000);

            } else {
                // 登录失败
                Log.i(TAG, "Login fail");
            }
        }

        @Override
        public void didBindDevice(GizWifiErrorCode result, String did) {
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 绑定成功
                Log.i(TAG, "Bind success");
                List<GizWifiDevice> list = GizWifiSDK.sharedInstance().getDeviceList();
                Log.i(TAG, "Device list "+list.size());
                if(list.size()>0){
                    GizWifiDevice device = list.get(0);
                    device.setListener(deviceListener);

                    device.setSubscribe(true);
                    //device.getHardwareInfo();

                }else{
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            List<GizWifiDevice> list = GizWifiSDK.sharedInstance().getDeviceList();
                            Log.i(TAG, "Device list2 "+list.size());

                            if(list.size()>0){
                                GizWifiDevice device = list.get(0);
                                device.setListener(deviceListener);

                                device.setSubscribe(true);
                                //device.getHardwareInfo();
                            }
                        }
                    },2000);
                }

            } else {
                // 绑定失败
                Log.i(TAG, "Bind fail");
            }
        }

        // 发现设备后更新(登录后或绑定后或主动刷新后)
        @Override
        public  void didDiscovered(GizWifiErrorCode result, List<GizWifiDevice> deviceList) {
            // 提示错误原因
            if(result != GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                Log.d("", "result: " + result.name());
            }

            // 显示变化后的设备列表
            Log.d("", "discovered deviceList: " + deviceList);
        }
    };

    GizWifiDeviceListener deviceListener = new GizWifiDeviceListener(){

        @Override
        public  void didSetSubscribe(GizWifiErrorCode result, GizWifiDevice device, boolean isSubscribed) {
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 订阅或解除订阅成功
                Log.i(TAG, "Subscribe success");

                ConcurrentHashMap<String, Object> command = new ConcurrentHashMap<> ();
                command.put("Test", true);
                device.write(command, 12);
            } else {
                // 失败
                Log.i(TAG, "Subscribe fail");
            }
        }

        @Override
        public  void didGetHardwareInfo(GizWifiErrorCode result, GizWifiDevice device, ConcurrentHashMap<String, String> hardwareInfo) {
            StringBuilder sb = new StringBuilder();
            if(result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                sb.append("Wifi Hardware Version:" + hardwareInfo.get("wifiHardVersion")
                        + "\r\n");
                sb.append("Wifi Software Version:" + hardwareInfo.get("wifiSoftVersion")
                        + "\r\n");
                sb.append("MCU Hardware Version:" + hardwareInfo.get("mcuHardVersion")
                        + "\r\n");
                sb.append("MCU Software Version:" + hardwareInfo.get("mcuSoftVersion")
                        + "\r\n");
                sb.append("Firmware Id:" + hardwareInfo.get("wifiFirmwareId") + "\r\n");
                sb.append("Firmware Version:" + hardwareInfo.get("wifiFirmwareVer")
                        + "\r\n");
                sb.append("Product Key:" + hardwareInfo.get("productKey") + "\r\n");
                sb.append("Device ID:" + device.getDid() + "\r\n");
                sb.append("Device IP:" + device.getIPAddress() + "\r\n");
                sb.append("Device MAC:" + device.getMacAddress() + "\r\n");

            }else{
                sb.append("获取失败，错误号：" + result);
            }

            Log.d(TAG,sb.toString());
        }

        @Override
        public  void didReceiveData(GizWifiErrorCode result, GizWifiDevice device, ConcurrentHashMap<String, Object> dataMap, int sn) {
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 操作成功
                Log.i(TAG, "SUCCESS("+sn+"): "+dataMap);

            } else {
                // 操作失败
                Log.i(TAG, "FAIL("+sn+")");
            }
        }

    };
}
