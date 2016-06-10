package com.dycode.mqttworkshop;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    private static final String TOPIC = "iotdevday-alpha/smartiotdemo/";
    private static final String TOPIC_CONTROL = "control";
    private static final String TOPIC_DATA = "data";


    private Switch switchStatus;
    private TextView textTemp;
    private Button btnLedControl;


    private MqttAndroidClient client;
    private MqttConnectOptions options;

    private int ledControl = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        switchStatus = (Switch) findViewById(R.id.switch_status_connection);
        switchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !client.isConnected())
                    connect();
                else if (!isChecked && client.isConnected())
                    disconnect();
            }
        });

        textTemp = (TextView) findViewById(R.id.text_temperature);
        btnLedControl = (Button) findViewById(R.id.btn_led_control);
        btnLedControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Control control = new Control(ledControl == 0 ? 1 : 0);
                String payload = new Gson().toJson(control);
                publish(TOPIC_CONTROL, payload);
            }
        });


        String clientId = MqttClient.generateClientId();
        options = new MqttConnectOptions();
        options.setUserName("iotdevday-alpha");
        options.setPassword("makanpadang".toCharArray());
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://iotid.cloudapp.net:1883",
                clientId);
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                switchStatus.setChecked(false);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String payload = new String(message.getPayload());
                if (topic.equals(TOPIC + TOPIC_DATA)) {
                    Toast.makeText(MainActivity.this, "Ada perubahan data " + payload, Toast.LENGTH_SHORT).show();
                    Data data = new Gson().fromJson(payload, Data.class);
                    textTemp.setText(String.valueOf(data.getRoomTemp()));
                } else if (topic.equals(TOPIC + TOPIC_CONTROL)) {
                    Toast.makeText(MainActivity.this, "Ada perubahan control " + payload, Toast.LENGTH_SHORT).show();
                    Control control = new Gson().fromJson(payload, Control.class);
                    ledControl = control.getLedControl();
                    btnLedControl.setText(control.getLedControl() == 1 ? "LED : ON" : "LED : OFF");

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });


    }


    private void connect() {
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    switchStatus.setChecked(true);
                    subscribe(TOPIC_DATA);
                    subscribe(TOPIC_CONTROL);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
                    switchStatus.setChecked(false);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try {
            IMqttToken token = client.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    switchStatus.setChecked(false);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    switchStatus.setChecked(false);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void subscribe(final String endtopic) {
        String topic = TOPIC + endtopic;
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    Toast.makeText(MainActivity.this, "Subscribe Success ke topic " + endtopic, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    Toast.makeText(MainActivity.this, "Failed Subcribe : " + endtopic, Toast.LENGTH_SHORT).show();


                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publish(String endTopic, String payload) {
        if(!client.isConnected()){
            Toast.makeText(MainActivity.this, "Please connect before publishing message", Toast.LENGTH_SHORT).show();
            return;
        }
        String topic = TOPIC + endTopic;
//        String payload = "{\"state\":1}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttToken token = client.publish(topic, message);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}