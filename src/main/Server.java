package main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Server {

    //private int processID;
    private String address;
    private int port;
    private List<Connection> connections;

    private boolean stopRequested;

    //public static final String REQUEST = "request";
    //public static final String OKAY = "okay";
    //public static final String TIMESTAMP = "timestamp";
    //public static final String REQUESTID = "requestID";
    public static final String SERVER_CONNECT = "server_connect";
    public static final String SERVER_BROADCAST = "server_broadcast";

    public Server() {

        this.connections = new ArrayList<Connection>();

        // Get address of localhost
        try {
            this.address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            System.out.println("Unable to get local host: " + e);
        }

        // Set port number to deafult
        this.port = 8890;

        this.stopRequested = false;

    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    // stops server AFTER the next client connection has been made or timeout is reached
    public void requestStop() {
        this.stopRequested = true;
    }

//    public int getProcessID() {
//        return processID;
//    }
//
//    public void setProcessID(int processID) {
//        this.processID = processID;
//    }
    // start the server if not already started and repeatedly listen for client connections until stop requested
    public void startServer() {

        // Reset stopRequested
        this.stopRequested = false;

        // Create new server socket
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000); // timeout for accept
            System.out.println("Server started at " + address + " on port " + port);
        } catch (IOException e) {
            System.err.println("Server can't listen on port: " + e);
            //System.exit(-1);
            return;
        }

        // Listen for connections, this blocks until a connection is made or the server socket times out.
        while (!stopRequested) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Server connection made with " + socket.getInetAddress().getHostAddress());

                // Create Connection
                Connection connection = new Connection(socket);

                // Send the new client process a series of join messages so that it can connect to other processes in system
                for (Connection conn : connections) {
                    connection.sendMessage(SERVER_CONNECT + " " + conn.getAddress());
                }

                // Add connection to list
                synchronized (connections) {
                    connections.add(connection);
                }

                // Start listning thread for connection
                Thread thread = new Thread(connection);
                thread.start();

            } catch (SocketTimeoutException e) {  // ignore and try again
            } catch (IOException e) {
                System.err.println("Can't accept client connection: " + e);
                stopRequested = true;
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {  // ignore
        }
        System.out.println("Server finishing");
    }

    // Connect to a specified server
    public void connect(String address) {
        try {
            //Create socket
            Socket socket = new Socket(address, port);
            System.out.println("Client connection made with " + socket.getInetAddress().getHostAddress());

            // Create connection and add to list
            Connection connection = new Connection(socket);
            synchronized (connections) {
                connections.add(connection);
            }

            // Start listning thread for connection
            Thread thread = new Thread(connection);
            thread.start();
        } catch (IOException e) {
            System.out.println("Unable to open client connection: " + e);
        }
    }

    public void broadcastMessage(String message) {

        synchronized (connections) {
            for (Connection conn : connections) {
                conn.sendMessage(SERVER_BROADCAST + " " + message);
            }
        }
    }

    public void receiveMessage(String message, Connection connection) {

        if (message.startsWith(SERVER_CONNECT)) {
            // Get the requested address of proces connection that wants to join
            String requestedAddress = message.substring(SERVER_CONNECT.length()).trim();

            // Check if requestedAddress is its own address
            boolean alreadyConnected = connection.getAddress().equals(requestedAddress);

            // Check whether there is already a connection with this address in the list
            synchronized (connections) {
                for (Connection conn : connections) {
                    if (conn.getAddress().equals(address)) {
                        alreadyConnected = true;
                    }
                }
            }

            // If not allredy connected, call connect method to connect to server and add to list of connections
            if (!alreadyConnected) {
                this.connect(address);
            } else {
                System.out.println("Allredy connected to server at " + requestedAddress);
            }
        } else if (message.startsWith(SERVER_BROADCAST)) {
            System.out.println("Broadcast message from " + connection.getAddress() + ": " + message.substring(SERVER_CONNECT.length()).trim());

        } else {
            System.out.println("Unknown type of message received: " + message);
        }
    }

}
