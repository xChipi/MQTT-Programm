package bbs2.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttConnectionManager {

    private MqttClient mqttClient;

    public void connectToMqtt(String brokerIp) {
        String broker = "tcp://" + brokerIp + ":1883";
        String clientId = "ExampleClient";

        try {
            mqttClient = new MqttClient(broker, clientId);
            mqttClient.connect();
            System.out.println("Connected to MQTT broker");
        } catch (MqttException e) {
            System.err.println("Error connecting to MQTT broker: " + e.getMessage());
        }
    }

    public MqttClient getMqttClient() {
        return mqttClient;
    }
}
