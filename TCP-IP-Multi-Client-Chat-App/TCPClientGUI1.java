package TCPIP_Connection_GUI;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class TCPClientGUI1 {
    private static JTextArea messageArea;
    private static JTextField inputField;
    private static JButton sendButton;
    private static PrintWriter out;
    private static BufferedReader in;
    private static String clientId;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Client");
        messageArea = new JTextArea(20, 50);
        messageArea.setEditable(false);
        inputField = new JTextField(40);
        sendButton = new JButton("Send");

        JPanel panel = new JPanel();
        panel.add(inputField);
        panel.add(sendButton);

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendClientMessage();
            }
        });

        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendClientMessage();
            }
        });

        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        String serverAddress = "localhost";
        int serverPort = 1234;

        try {
            Socket socket = new Socket(serverAddress, serverPort);
            messageArea.append("Connected to the server!\n");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Assign a client ID
            clientId = JOptionPane.showInputDialog(frame, "Enter your client ID:");
            out.println(clientId);

        //manages the input 
            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        messageArea.append(serverMessage + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendClientMessage() {
        String userMessage = inputField.getText();
        inputField.setText("");
        if (!userMessage.isEmpty()) {
            out.println(userMessage);  // Send the raw message to the server
        }
    }
}
