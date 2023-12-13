package bbs2.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MqttGui extends JFrame {

    private MqttConnectionManager connectionManager;
    private JTextField brokerIpField;
    private JTextField topicField;
    private JTextField messageField;
    private JButton connectButton;
    private JButton sendButton;
    private JButton subscribeButton;

    public MqttGui() {
        super("MQTT GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLayout(new GridLayout(5, 2));

        connectionManager = new MqttConnectionManager();

        brokerIpField = new JTextField();
        topicField = new JTextField();
        messageField = new JTextField();
        connectButton = new JButton("Connect");
        sendButton = new JButton("Send");
        subscribeButton = new JButton("Subscribe"); // Neu: Button fürs Subskribieren

        add(new JLabel("Broker IP:"));
        add(brokerIpField);
        add(new JLabel("Topic:"));
        add(topicField);
        add(new JLabel("Message:"));
        add(messageField);
        add(connectButton);
        add(sendButton);
        add(subscribeButton); // Neu: Button fürs Subskribieren

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToMqtt();
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        subscribeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                subscribeToTopic();
            }
        });
    }

    private void connectToMqtt() {
        String brokerIp = brokerIpField.getText();
        connectionManager.connectToMqtt(brokerIp);
    }

    private void sendMessage() {
        MqttClient mqttClient = connectionManager.getMqttClient();

        if (mqttClient != null && mqttClient.isConnected()) {
            String topic = topicField.getText();
            String message = messageField.getText();

            connectionManager.publishMessage(topic, message);
        } else {
            JOptionPane.showMessageDialog(this, "Not connected to MQTT broker", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void subscribeToTopic() {
        MqttClient mqttClient = connectionManager.getMqttClient();

        if (mqttClient != null && mqttClient.isConnected()) {
            String topic = topicField.getText();
            connectionManager.subscribeToTopic(topic);
        } else {
            JOptionPane.showMessageDialog(this, "Not connected to MQTT broker", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MqttGui mqttGui = new MqttGui();
                mqttGui.setVisible(true);
            }
        });
    }
}