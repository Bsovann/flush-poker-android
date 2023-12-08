package com.example.flush_poker_android.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/** Unused class, originally meant to serve as a service that processes player actions
 *
 * @author Kyle Chainey*/
public class MessageService  {

    private static final int SERVER_PORT = 8888;

    /**
     * Starts the server thread.
     */
    public void startServer() {
        new ServerThread().start();
    }

    /**
     * Connects to the server.
     *
     * @param hostAddress The IP address of the group owner (server).
     */
    public void startClient(String hostAddress) {
        new ClientThread(hostAddress).start();
    }

    /**
     * Server thread to handle incoming client connections.
     */
    private class ServerThread extends Thread {
        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
                while (!Thread.currentThread().isInterrupted()) {
                    Socket clientSocket = serverSocket.accept();
                    new ClientHandlerThread(clientSocket).start();
                }
            } catch (IOException e) {
                // Handle exceptions
            }
        }
    }

    /**
     * Client thread to connect to the server.
     */
    private class ClientThread extends Thread {
        private final String hostAddress;

        public ClientThread(String hostAddress) {
            this.hostAddress = hostAddress;
        }

        @Override
        public void run() {
            try (Socket socket = new Socket(hostAddress, SERVER_PORT)) {
                // Handle socket connection for sending and receiving messages
                handleSocketConnection(socket);
            } catch (IOException e) {
                // Handle exceptions
            }
        }
    }

    /**
     * Thread to handle communication with a connected client.
     */
    private class ClientHandlerThread extends Thread {
        private final Socket clientSocket;

        public ClientHandlerThread(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                handleSocketConnection(clientSocket);
            } catch (IOException e) {
                // Handle exceptions
            }
        }
    }

    /**
     * Handles the socket connection for sending and receiving messages.
     *
     * @param socket The socket connection.
     */
    private void handleSocketConnection(Socket socket) throws IOException {
        // Create input and output streams
        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // Example: Send a message
        writer.println("Hello from " + (socket.isClosed() ? "Client" : "Server"));

        // Example: Read a message
        String message;
        while ((message = reader.readLine()) != null) {
            handleMessage(message);
        }
    }

    /**
     * Handles a received message.
     *
     * @param message The received message.
     */
    private void handleMessage(String message) {
        // Process the received message
    }

    /**
     * Send a message to a connected peer.
     *
     * @param message The message to send.
     * @param socket  The socket representing the connection to the peer.
     */
    public void sendMessage(String message, Socket socket) {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(message);
        } catch (IOException e) {
            // Handle exceptions
        }
    }
}
