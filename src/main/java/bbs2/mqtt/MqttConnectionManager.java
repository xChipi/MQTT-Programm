package bbs2.mqtt;

import org.eclipse.paho.client.mqttv3.*;

public class MqttConnectionManager {

    private MqttClient mqttClient;
    private MessageListener messageListener;

    public interface MessageListener {
        void onMessageReceived(String topic, String message);
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public void connectToMqtt(String brokerIp) {
        String broker = "tcp://" + brokerIp + ":1883";
        String clientId = "ExampleClient";

        try {
            mqttClient = new MqttClient(broker, clientId);
            mqttClient.connect();
            System.out.println("Connected to MQTT broker");

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.err.println("Connection lost: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("Message received on topic: " + topic);
                    System.out.println("Message: " + new String(message.getPayload()));

                    if (messageListener != null) {
                        messageListener.onMessageReceived(topic, new String(message.getPayload()));
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Wird aufgerufen, wenn die Zustellung der Nachricht abgeschlossen ist
                }
            });
        } catch (MqttException e) {
            System.err.println("Error connecting to MQTT broker: " + e.getMessage());
        }
    }

    public void publishMessage(String topic, String message) {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                mqttMessage.setRetained(true);
                mqttClient.publish(topic, mqttMessage);
                System.out.println("Message published successfully");
            } catch (MqttException e) {
                System.err.println("Error publishing message: " + e.getMessage());
            }
        } else {
            System.err.println("Not connected to MQTT broker");
        }
    }

    public void subscribeToTopic(String topic) {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.subscribe(topic);
                System.out.println("Subscribed to topic: " + topic);
            } catch (MqttException e) {
                System.err.println("Error subscribing to topic: " + e.getMessage());
            }
        } else {
            System.err.println("Not connected to MQTT broker");
        }
    }

    public MqttClient getMqttClient() {
        return mqttClient;
    }
}
