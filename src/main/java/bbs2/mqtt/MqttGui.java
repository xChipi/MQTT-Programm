package bbs2.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

public class MqttGui extends JFrame {

    private MqttConnectionManager connectionManager;
    private JTextField brokerIpField;
    private JTextField topicField;
    private JTextField messageField;
    private JTextField howOftenField;
    private JButton connectButton;
    private JButton sendButton;
    private JButton subscribeButton;
    private JLabel statusLabel;
    private JLabel publishOutputLabel;
    private List<String> lastMessages; // Neu: Liste für die letzten Nachrichten


    public MqttGui() {
        super("MQTT GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 1000);
        setLayout(new GridLayout(7, 2)); // adjust grid

        connectionManager = new MqttConnectionManager();
        lastMessages = new ArrayList<>(); // Neu: Initialisiere die Liste

        brokerIpField = new JTextField();
        topicField = new JTextField();
        messageField = new JTextField();
        howOftenField = new JTextField();
        connectButton = new JButton("Connect");
        sendButton = new JButton("Send");
        subscribeButton = new JButton("Subscribe");
        statusLabel = new JLabel("");
        publishOutputLabel = new JLabel("");

        add(new JLabel("Broker IP:"));
        add(brokerIpField);
        add(new JLabel("Topic:"));
        add(topicField);
        add(new JLabel("Message:"));
        add(messageField);
        add(new JLabel("How often:"));
        add(howOftenField);
        add(connectButton);
        add(sendButton);
        add(subscribeButton);
        add(statusLabel);
        add(new JLabel("received messages:"));
        statusLabel.setPreferredSize(new Dimension(300, 300)); // Neu: Setze die Größe des Labels
        add(publishOutputLabel);
        publishOutputLabel.setPreferredSize(new Dimension(300, 300)); // Neu: Setze die Größe des Labels


        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToMqtt();
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int amount;
                try {
                    amount = parseInt(howOftenField.getText());
                } catch (NumberFormatException ex) {
                    amount = 1;
                }
                for (int i = 0; i < amount; i++) {
                    sendMessage();
                }
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
        statusLabel.setText("Connected to MQTT broker");
    }

    private void sendMessage() {
        MqttClient mqttClient = connectionManager.getMqttClient();

        if (mqttClient != null && mqttClient.isConnected()) {
            String topic = topicField.getText();
            String message = messageField.getText();

            connectionManager.publishMessage(topic, message);

            // Neu: Hinzufügen der veröffentlichten Nachricht zur Liste
            lastMessages.add("Published on topic: " + topic + ", Message: " + message);

            // Neu: Begrenze die Liste auf die letzten 10 Nachrichten
            if (lastMessages.size() > 6) {
                lastMessages.remove(0);
            }

            // Neu: Aktualisiere die Anzeige der letzten Nachrichten
            updateLastMessagesLabel();

            statusLabel.setText("Message sent successfully");
        } else {
            JOptionPane.showMessageDialog(this, "Not connected to MQTT broker", "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Failed to send message");
        }
    }

    private void subscribeToTopic() {
        MqttClient mqttClient = connectionManager.getMqttClient();

        if (mqttClient != null && mqttClient.isConnected()) {
            String topic = topicField.getText();
            connectionManager.subscribeToTopic(topic);
            statusLabel.setText("Subscribed to topic: " + topic);
        } else {
            JOptionPane.showMessageDialog(this, "Not connected to MQTT broker", "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Failed to subscribe to topic");
        }
    }

    // Neu: Methode zur Aktualisierung der Anzeige der letzten Nachrichten
    private void updateLastMessagesLabel() {
        StringBuilder messages = new StringBuilder("<html>");
        for (String msg : lastMessages) {
            messages.append(msg).append("<br>");
        }
        messages.append("</html>");
        publishOutputLabel.setText(messages.toString());
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
