package TCPIP_Connection_GUI;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class TCPServerGUI {
    private static Map<Integer, ClientHandler> clientHandlers = new HashMap<>();
    private static JTextArea messageArea;
    private static JTextField inputField;
    private static JButton sendButton;
    private static int clientCounter = 0;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Server");
        messageArea = new JTextArea(20, 50);
        messageArea.setEditable(false);
        inputField = new JTextField(40);
        sendButton = new JButton("Send");

        JPanel panel = new JPanel();
        panel.add(inputField);
        panel.add(sendButton);

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendServerMessage();
            }
        });

        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendServerMessage();
            }
        });

        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try (ServerSocket serverSocket = new ServerSocket(1234)) {
            messageArea.append("Server started and waiting for clients...\n");

            while (true) {
                Socket socket = serverSocket.accept();
                clientCounter++;
                messageArea.append("New client connected!\n");

                ClientHandler clientHandler = new ClientHandler(socket, clientCounter);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendServerMessage() {
        String serverMessage = inputField.getText();
        inputField.setText("");
        if (!serverMessage.isEmpty()) {
            String formattedMessage = "Server: " + serverMessage;
            messageArea.append(formattedMessage + "\n");
            broadcastMessage(formattedMessage);
        }
    }

    static void broadcastMessage(String message) {
        for (ClientHandler handler : clientHandlers.values()) {
            handler.sendMessage(message);
        }
    }

    static void registerClient(int clientId, ClientHandler handler) {
        clientHandlers.put(clientId, handler);
    }

    static void deregisterClient(int clientId) {
        clientHandlers.remove(clientId);
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private int clientId;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                TCPServerGUI.registerClient(clientId, this);

                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    if (clientMessage.equals("-1")) {
                        messageArea.append("Client " + clientId + " disconnected.\n");
                        break;
                    }

                    String formattedMessage = "Client " + clientId + ": " + clientMessage;
                    messageArea.append(formattedMessage + "\n");
                    TCPServerGUI.broadcastMessage(formattedMessage);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                TCPServerGUI.deregisterClient(clientId);
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}