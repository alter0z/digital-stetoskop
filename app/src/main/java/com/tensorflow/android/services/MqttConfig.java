package com.tensorflow.android.services;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class MqttConfig {
//    private static final String HTTP_ADDRESS = "http address";
    private static final String MQTT_ADDRESS = "mqtt address";

//    public static String getHttpAddress(Context context) {
//        SharedPreferences pref = context.getSharedPreferences(HTTP_ADDRESS, Context.MODE_PRIVATE);
//        String ip = pref.getString("ip", "telemedicine.co.id:3000");
//        if (ip.equals(""))
//            ip = context.getString(R.string.server_ip_address);
//        return String.format(Locale.US, context.getString(R.string.http_url), ip);
////        return String.format(Locale.US, "http://%s/mobileapi", ip);
////        return String.format(Locale.US, "http://%s/mobileapi", ip);
//    }

    public static String getMqttAddress(Context context) {
        SharedPreferences pref = context.getSharedPreferences(MQTT_ADDRESS, Context.MODE_PRIVATE);
        String ip = pref.getString("mqttserver", "103.161.184.235");
        String port = pref.getString("port", "1883"); // 49877
        if (ip.equals(""))
            ip = context.getString(com.ansori.mqtt.R.string.server_ip_address);
        if (port.equals(""))
            port = context.getString(com.ansori.mqtt.R.string.server_http_port);
//        return String.format(Locale.US, context.getString(R.string.mqtt_url), ip);
//        String ip = "telemedicinecoid-in.cloud.revoluz.io";
//        String port = "49560";
        return String.format(Locale.US, "tcp://%s:%s", ip, port);
    }
}
