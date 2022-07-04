package com.ansori.mqtt;

import android.content.Context;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class MqttClient {
    private final MqttAndroidClient client;
//    MqttConnectOptions options;
    IMqttToken token;

    public MqttClient(Context context) {
        String serverUri = MqttConfig.getMqttAddress(context);
        String clientID = org.eclipse.paho.client.mqttv3.MqttClient.generateClientId();
//        String username = "an username";
//        String password = "an password";

        Random random = new Random();

        client = new MqttAndroidClient(context, serverUri, clientID);
//        options = new MqttConnectOptions();
//        options.setUserName(username);
//        options.setPassword(password.toCharArray());

        try {
            token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void getPublish(Context context, String topic, String message) {
        try {
            client.publish(topic,message.getBytes(StandardCharsets.UTF_8),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT).show();
        }
    }
}
