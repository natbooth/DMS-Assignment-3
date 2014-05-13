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
    
    private int processID;
    private boolean coordinator;
    private int timestamp;
    private String address;
    private int port;
    private boolean stopRequested;
    private List<Connection> connections;

    //public static final String REQUEST = "request";
    //public static final String OKAY = "okay";
    //public static final String TIMESTAMP = "timestamp";
    //public static final String REQUESTID = "requestID";
    public static final String SERVER_CONNECT = "server_connect";
    public static final String SERVER_SETPROCESSID = "server_setprocessid";
    public static final String SERVER_PROCESSID = "server_processid";
    public static final String SERVER_COORDINATOR = "server_coordinator";
    public static final String SERVER_TIMESTAMP = "server_timestamp";
    public static final String SERVER_BROADCAST = "server_broadcast";
    public static final String ELECTION_ANNOUNCE = "election_announce";
    public static final String ELECTION_OK = "election_ok";
    public static final String ELECTION_COORDINATOR = "election_coordinator";
    
    public Server() {
        this.timestamp = 0;
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
    
    public int getProcessID() {
        return processID;
    }
    
    public void setProcessID(int processID) {
        this.processID = processID;
    }
    
    public boolean isCoordinator() {
        return coordinator;
    }
    
    public void setCoordinator(boolean coordinator) {
        this.coordinator = coordinator;
    }
    
    public int getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getAddress() {
        return this.address;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }

    // Start the server if not already started and repeatedly listen for client connections until stop requested
    public void startServer(boolean coordinator) {

        // Set Coordinator
        this.coordinator = coordinator;

        // Set processID
        if (coordinator) {
            this.processID = 1;
        }

        // Reset stopRequested
        this.stopRequested = false;

        // Create new server socket
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000);
            System.out.println("Server started at " + address + " on port " + port);
        } catch (IOException e) {
            System.err.println("Can't listen on port: " + e + ". Server NOT started.");
            //System.exit(-1);
            this.stopRequested = true;
            return;
        }

        // Create list for connections
        this.connections = new ArrayList<Connection>();

        // Listen for connections, this blocks until a connection is made or the server socket times out.
        while (!stopRequested) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Server connection made with " + socket.getInetAddress().getHostAddress());

                // Create Connection
                Connection connection = new Connection(this, 0, false, 0, socket);

                // Send this servers details
                connection.sendMessage(SERVER_SETPROCESSID + " " + this.getNewProcessID());
                connection.sendMessage(SERVER_PROCESSID + " " + this.processID);
                connection.sendMessage(SERVER_TIMESTAMP + " " + this.timestamp);
                connection.sendMessage(SERVER_COORDINATOR + " " + this.getCoordinatorID());

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
                
            } catch (SocketTimeoutException e) {  // Catch timeout and continue
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

    // Stops server AFTER the next client connection has been made or timeout is reached
    public void stopServer() {
        this.stopRequested = true;
    }

    // Connect to a specified server
    public void connect(String address) {
        try {
            // Create socket
            Socket socket = new Socket(address, port);
            System.out.println("Client connection made with " + socket.getInetAddress().getHostAddress());

            // Create connection and add to list
            Connection connection = new Connection(this, 0, false, 0, socket);
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
    
    public void disconnect(Connection connection) {
        if (connection == null) {
            connection = this.connections.get(0);
        }
        // Request stop and remove from list
        connection.requestStop();
        synchronized (connections) {
            connections.remove(connection);
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
            System.out.println("Broadcast message from " + connection.getAddress() + ": " + message.substring(SERVER_BROADCAST.length()).trim());
        } else if (message.startsWith(SERVER_SETPROCESSID)) {
            System.out.println("Message from " + connection.getAddress() + ": " + message.substring(SERVER_SETPROCESSID.length()).trim());
            this.setProcessID(Integer.parseInt(message.substring(SERVER_SETPROCESSID.length()).trim()));
        } else if (message.startsWith(SERVER_PROCESSID)) {
            connection.setProcessID(Integer.parseInt(message.substring(SERVER_SETPROCESSID.length()).trim()));
            System.out.println("Message from " + connection.getAddress() + ": " + message.substring(SERVER_PROCESSID.length()).trim());
        } else if (message.startsWith(SERVER_COORDINATOR)) {
            // Set server coordinator
            for (Connection conn : connections) {
                if (conn.getProcessID() == Integer.parseInt(message.substring(SERVER_COORDINATOR.length()).trim())) {
                    conn.setCoordinator(true);
                }
            }
            System.out.println("Message from " + connection.getAddress() + ": " + message.substring(SERVER_COORDINATOR.length()).trim());
        } else if (message.startsWith(SERVER_TIMESTAMP)) {
            connection.setTimestamp(Integer.parseInt(message.substring(SERVER_SETPROCESSID.length()).trim()));
            System.out.println("Message from " + connection.getAddress() + ": " + message.substring(SERVER_TIMESTAMP.length()).trim());
            
        } else {
            System.out.println("Unknown type of message received: " + message);
        }
    }
    
    private int getCoordinatorID() {
        // Check if this server is corordernator else check throug list of connections
        if (this.isCoordinator()) {
            return this.processID;
        } else {
            for (Connection connection : connections) {
                if (connection.isCoordinator()) {
                    return connection.getProcessID();
                }
            }
        }
        // If no leader is found return -1
        return -1;
    }
    
    private int getNewProcessID() {
        int highestProcessID = this.getProcessID();
        
        for (Connection connection : connections) {
            if (connection.getProcessID() > highestProcessID) {
                highestProcessID = connection.getProcessID();
            }
        }
        
        return highestProcessID + 1;
    }
    
}
