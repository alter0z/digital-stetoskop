package com.tensorflow.android.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.tensorflow.android.example.stetoskopdigital1.Btreceiver;

//import org.eclipse.paho.android.service.MqttAndroidClient;
import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import kotlin.jvm.internal.Intrinsics;

public class MqttClient {
    private final MqttAndroidClient client;
    MqttConnectOptions options;
    IMqttToken token;

    @RequiresApi(api = Build.VERSION_CODES.S)
    public MqttClient(Context context) {
        String serverUri = MqttConfig.getMqttAddress(context);
        String clientID = org.eclipse.paho.client.mqttv3.MqttClient.generateClientId();
        String username = "mosquitto";
        String password = "rootspace2022";
        System.out.println(serverUri);

        Random random = new Random();

        client = new MqttAndroidClient(context, "tcp://broker.hivemq.com:1883"/*serverUri*/, username+"uadshowaidhoqwwhoduqwhd"+random.nextInt(), Ack.AUTO_ACK);
        options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());

        token = client.connect(options, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
                System.out.println("Connected");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Toast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT).show();
                System.out.println("Failed to connect");
            }
        });
//            token.setActionCallback(new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Toast.makeText(context, "Failed to connect", Toast.LENGTH_SHORT).show();
//                }
//            });

        // pending intent
        PendingIntent broadcast;
        broadcast = PendingIntent.getBroadcast(context, 0, new Intent(context, Btreceiver.class), PendingIntent.FLAG_MUTABLE);
        Intrinsics.checkExpressionValueIsNotNull(broadcast, "PendingIntent.getBroadca…ionContext, 0, intent, 0)");
        Intrinsics.checkExpressionValueIsNotNull(broadcast, "Intent(applicationContex…, 0, intent, 0)\n        }");
    }

    public void getPublish(Context context, String topic, String message) {
        client.publish(topic,message.getBytes(StandardCharsets.UTF_8),0,false);
    }
}
