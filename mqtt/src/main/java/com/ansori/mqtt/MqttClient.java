package com.ansori.mqtt;

import android.content.Context;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.nio.charset.StandardCharsets;

public class MqttClient {
    private final MqttAndroidClient client;
    MqttConnectOptions options;
    IMqttToken token;

    public MqttClient(Context context) {
        String serverUri = "the server uri";
        String clientID = "the client id";
        String username = "an username";
        String password = "an password";

        client = new MqttAndroidClient(context, serverUri, clientID);
        options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());

        try {
            token = client.connect(options);
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
