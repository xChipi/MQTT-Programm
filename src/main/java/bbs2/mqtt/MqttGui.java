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


//////////////////////////////////////////////////////////////////////////////////////////
// Beschreibung des Programms:                                                          //
// Dieses Program ermöglicht es auf einem MQTT Broker zu subscriben und zu publishen.   //
// Es gibt mehrere Funktionen, die er erleichtern den MQTT zu verwenden                 //
// In Broker IP gibt man seine IP ein. (test.mosquitto.org kann immer benutzt werden)   //
// In Topic gibt man sein Thema an (z.B. Zimmertemperatur)                              //
// In Message gibt man seine Nachticht an (z.B. 22°C)                                   //
// In Random Number kann man eine Range an Zahlen angeben (z.B. 1-10)                   //
// Das Programm wird zufällig Zahlen aus der Range auswählen.                           //
// Das wird nur verwendet, wenn Message leer ist.                                       //
// In How Often kann man angeben, wie Oft eine Nachricht verschickt werden soll         //
// How Often kann auch mit der Random Number verwendet werden.                          //
// Mit Connect verbindet man sich, dass muss vorher immer passieren                     //
// Mit Send published man eine Message an den MQTT                                      //
// Mit Subscribe kann man das Thema in "Topic" subscriben                               //
// In received Message Sieht man die Nachrichten, die gepublished wurden                //
// Es werden bis zu 6 Zwischengespeichert und von unten die neusten angezeigt           //
//////////////////////////////////////////////////////////////////////////////////////////

public class MqttGui extends JFrame {

    private MqttConnectionManager connectionManager;
    private JTextField brokerIpField;
    private JTextField topicField;
    private JTextField messageField;
    private JTextField rangeField;
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
        setSize(900, 1000);
        setLayout(new GridLayout(8, 2)); // adjust grid

        connectionManager = new MqttConnectionManager();
        lastMessages = new ArrayList<>(); // Neu: Initialisiere die Liste

        brokerIpField = new JTextField();
        topicField = new JTextField();
        messageField = new JTextField();
        rangeField = new JTextField();
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
        add(new JLabel("Random number from Range (e.g., \"1-5\"), only aplies if message is empty:"));
        add(rangeField);
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
                if (messageField.getText().isEmpty()) {
                    handleRangeInput(); // Neu: Verarbeite die Range-Eingabe
                } else {
                    int amount;
                    try {
                        amount = parseInt(howOftenField.getText());
                    } catch (NumberFormatException ex) {
                        amount = 1;
                    }
                    for (int i = 0; i < amount; i++) {
                        sendMessage(messageField.getText());
                    }
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

    private void sendMessage(String msg) {
        MqttClient mqttClient = connectionManager.getMqttClient();

        if (mqttClient != null && mqttClient.isConnected()) {
            String topic = topicField.getText();
            connectionManager.publishMessage(topic, msg);

            // Neu: Hinzufügen der veröffentlichten Nachricht zur Liste
            lastMessages.add("Published on topic: " + topic + ", Message: " + msg);

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

    private void handleRangeInput() {
        String rangeInput = rangeField.getText();
        if (!rangeInput.isEmpty()) {
            String[] rangeArray = rangeInput.split("-");
            if (rangeArray.length == 2) {
                try {
                    int start = Integer.parseInt(rangeArray[0]);
                    int end = Integer.parseInt(rangeArray[1]);

                    int amount;
                    try {
                        amount = parseInt(howOftenField.getText());
                    } catch (NumberFormatException ex) {
                        amount = 1;
                    }

                    for (int i = 0; i < amount; i++) {
                        int randomValue = (int) (Math.random() * (end - start + 1) + start);
                        sendMessage(String.valueOf(randomValue));
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid range format", "Error", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Failed to send message");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid range format", "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Failed to send message");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Range cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Failed to send message");
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
