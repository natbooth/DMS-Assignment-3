package main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the server class.
 */
public class Server extends Thread implements RMIClient {
    
    private Map<Integer, Integer> vectorTimestamp;
    private int processID;
    private boolean coordinator;
    private int timestamp;
//    private String address;
//    private int port;
    private boolean stopRequested;
//    private List<Connection> connections;
    private Map<Integer, RMIClient> clients;
    private RMIClient leader;
    private List<String> fileList;
    private boolean electionInProgress;

    //public static final String REQUEST = "request";
    //public static final String OKAY = "okay";
    //public static final String TIMESTAMP = "timestamp";
    //public static final String REQUESTID = "requestID";
//    public static final String SERVER_CONNECT = "server_connect";
//    public static final String SERVER_SETPROCESSID = "server_setprocessid";
//    public static final String SERVER_PROCESSID = "server_processid";
//    public static final String SERVER_COORDINATOR = "server_coordinator";
//    public static final String SERVER_TIMESTAMP = "server_timestamp";
//    public static final String SERVER_BROADCAST = "server_broadcast";
//    public static final String ELECTION_ANNOUNCE = "election_announce";
//    public static final String ELECTION_OK = "election_ok";
//    public static final String ELECTION_COORDINATOR = "election_coordinator";
    
    public Server() {
        this.timestamp = 0;
        this.vectorTimestamp = new HashMap<>();
        this.clients = new HashMap<>();
        // Get address of localhost
//        try {
//            this.address = InetAddress.getLocalHost().getHostAddress();
//        } catch (UnknownHostException e) {
//            System.out.println("Unable to get local host: " + e);
//        }
        // Set port number to deafult
//        this.port = 8891;
        
    }    
  
    private List<String> getLocalFileList()
    {
        List<String> files = new ArrayList<>();
        //TODO get all files
        return files;
    }
    
    /**
     * TODO devise a way to decide what files are the newest.
     * TODO handle deleted files?
     * @param localFiles
     * @param remoteFiles
     * @return 
     */
    private static List<String> compareFileList(List<String> localFiles, List<String> remoteFiles)
    {
        List<String> resultList = new ArrayList<>();
        
        
        return resultList;
    }
    
    public void setClients(Map<Integer, RMIClient> clients)
    {
        //remove self
        this.clients.remove(processID);
        this.clients = clients;        
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
//    
//    public String getAddress() {
//        return this.address;
//    }
//    
//    public int getPort() {
//        return port;
//    }
//    
//    public void setPort(int port) {
//        this.port = port;
//    }

    // Start the server if not already started and repeatedly listen for client connections until stop requested
    public void initializeServer(RMIClient leader) {

        // Set Coordinator
        this.leader = leader;

        // Reset stopRequested
        this.stopRequested = false;

        // Create new server socket
//        ServerSocket serverSocket = null;
//        try {
//            serverSocket = new ServerSocket(port);
//            serverSocket.setSoTimeout(1000);
//            System.out.println("Server started at " + address + " on port " + port);
//        } catch (IOException e) {
//            System.err.println("Can't listen on port: " + e + ". Server NOT started.");
//            //System.exit(-1);
//            this.stopRequested = true;
//            return;
//        }

        // Create list for connections
//        this.connections = new ArrayList<>();

        // Listen for connections, this blocks until a connection is made or the server socket times out.
//        while (!stopRequested) {
//            try {
//                Socket socket = serverSocket.accept();
//                System.out.println("Server connection made with " + socket.getInetAddress().getHostAddress());
//
//                // Create Connection
//                Connection connection = new Connection(this, 0, false, 0, socket);
//
//                // Send this servers details
//                connection.sendMessage(SERVER_SETPROCESSID + " " + this.getNewProcessID());
//                connection.sendMessage(SERVER_PROCESSID + " " + this.processID);
//                connection.sendMessage(SERVER_TIMESTAMP + " " + this.timestamp);
//                connection.sendMessage(SERVER_COORDINATOR + " " + this.getCoordinatorID());
//
//                // Send the new client process a series of join messages so that it can connect to other processes in system
//                for (Connection conn : connections) {
//                    connection.sendMessage(SERVER_CONNECT + " " + conn.getAddress());
//                }
//
//                // Add connection to list
//                synchronized (connections) {
//                    connections.add(connection);
//                }
//
//                // Start listning thread for connection
//                Thread thread = new Thread(connection);
//                thread.start();
//                
//            } catch (SocketTimeoutException e) {  // Catch timeout and continue
//            } catch (IOException e) {
//                System.err.println("Can't accept client connection: " + e);
//                stopRequested = true;
//            }
//        }
//        try {
//            serverSocket.close();
//        } catch (IOException e) {  // ignore
//        }
//        System.out.println("Server finishing");
    }

    @Override
    public void run()
    {
        System.out.println("Starting server thread.");
        
        List<String> localList = getLocalFileList();
        // Set processID
        if (this == leader) 
        {
            this.processID = 1;
        } else
        {
            try {
                this.clients = leader.getClients();
                this.clients.put(leader.getProcessID(), leader);
                this.processID = getNewProcessID();
                System.out.println("Client was given Process ID #" + this.processID);
                fileList = leader.getFileList();
                fileList = compareFileList(localList, fileList);
                leader.updateFileList(fileList);
            } catch (RemoteException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Server setup complete.");
        while (!stopRequested)
        {
            
        }
    }
    
    // Stops server AFTER the next client connection has been made or timeout is reached
    public void stopServer() {
        this.stopRequested = true;
    }

    // Connect to a specified server
//    public void connect(String address) {
//        try {
//            // Create socket
//            Socket socket = new Socket(address, port);
//            System.out.println("Client connection made with " + socket.getInetAddress().getHostAddress());
//
//            // Create connection and add to list
//            Connection connection = new Connection(this, 0, false, 0, socket);
//            synchronized (connections) {
//                connections.add(connection);
//            }
//
//            // Start listning thread for connection
//            Thread thread = new Thread(connection);
//            thread.start();
//        } catch (IOException e) {
//            System.out.println("Unable to open client connection: " + e);
//        }
//    }
//    
//    public void disconnect(Connection connection) {
//        if (connection == null) {
//            connection = this.connections.get(0);
//        }
//        // Request stop and remove from list
//        connection.requestStop();
//        synchronized (connections) {
//            connections.remove(connection);
//        }
//    }
        
    private void startElection()
    {
        if (!electionInProgress)
        {
            //TODO remove the old leader from client list FIRST
            increaseVTimestamp();
            electionInProgress = true;
            int bestLeaderID = processID;
            try {
                bestLeaderID = getBestLeader(processID);
            } catch (RemoteException ex) {} //local, can't happen
            List<Integer> peerVotes = new ArrayList<>();
            for (RMIClient client : clients.values())
            {
                try {
                    client.setTimestamp(vectorTimestamp);
                    peerVotes.add(client.getBestLeader(bestLeaderID));
                } catch (RemoteException e) {
                    System.err.println("Error connecting to client: " + e);
                }            
            }
            if (peerVotes.size() == clients.size()) //TODO can or SHOULD this happen?  If so, map of client replies might be better?
            {
                bestLeaderID = 0;
                for (int i : peerVotes)
                {
                    if (i > bestLeaderID)
                    {
                        bestLeaderID = i;
                    }
                }
                leader = clients.get(bestLeaderID);
                for (RMIClient client : clients.values())
                {
                    try {
                        client.setLeader(bestLeaderID);
                    } catch (RemoteException e) {
                        System.err.println("Error connecting to client: " + e);
                    }            
                }
            }
        }
    }
    
    public void broadcastMessage(String message) {
        
        switch (message.toLowerCase()) 
        {
            case "election" :
                startElection();
                break;
                
            default :
                System.out.println("Command '" + message + "' not recognised.");
        }
    }
    
//    public void receiveMessage(String message, Connection connection) {
//        
//        if (message.startsWith(SERVER_CONNECT)) {
//            // Get the requested address of proces connection that wants to join
//            String requestedAddress = message.substring(SERVER_CONNECT.length()).trim();
//
//            // Check if requestedAddress is its own address
//            boolean alreadyConnected = connection.getAddress().equals(requestedAddress);
//
//            // Check whether there is already a connection with this address in the list
//            synchronized (connections) {
//                for (Connection conn : connections) {
//                    if (conn.getAddress().equals(address)) {
//                        alreadyConnected = true;
//                    }
//                }
//            }
//
//            // If not allredy connected, call connect method to connect to server and add to list of connections
//            if (!alreadyConnected) {
//                this.connect(address);
//            } else {
//                System.out.println("Allredy connected to server at " + requestedAddress);
//            }
//        } else if (message.startsWith(SERVER_BROADCAST)) {
//            System.out.println("Broadcast message from " + connection.getAddress() + ": " + message.substring(SERVER_BROADCAST.length()).trim());
//        } else if (message.startsWith(SERVER_SETPROCESSID)) {
//            this.setProcessID(Integer.parseInt(message.substring(SERVER_SETPROCESSID.length()).trim()));
//        } else if (message.startsWith(SERVER_PROCESSID)) {
//            connection.setProcessID(Integer.parseInt(message.substring(SERVER_PROCESSID.length()).trim()));
//        } else if (message.startsWith(SERVER_COORDINATOR)) {
//            // Set server coordinator
//            for (Connection conn : connections) {
//                if (conn.getProcessID() == Integer.parseInt(message.substring(SERVER_COORDINATOR.length()).trim())) {
//                    conn.setCoordinator(true);
//                }
//            }
//        } else if (message.startsWith(SERVER_TIMESTAMP)) {
//            connection.setTimestamp(Integer.parseInt(message.substring(SERVER_TIMESTAMP.length()).trim())); 
//        } else {
//            System.out.println("Unknown type of message received: " + message);
//        }
//    }
//    
//    private int getCoordinatorID() {
//        // Check if this server is co-ordinator else check throug list of connections
//        if (this.isCoordinator()) {
//            return this.processID;
//        } else {
//            for (Connection connection : connections) {
//                if (connection.isCoordinator()) {
//                    return connection.getProcessID();
//                }
//            }
//        }
//        // If no leader is found return -1
//        return -1;
//    }
    
    private int getNewProcessID() {
        int highestProcessID = 0;
        try {
            highestProcessID = this.getProcessID();
        
            for (RMIClient client : clients.values()) {
                if (client.getProcessID() > highestProcessID) {
                    highestProcessID = client.getProcessID();
                }
            }
        } catch (RemoteException ex) {
            System.err.println("Failed to get process id. ");
        }
        
        return highestProcessID + 1;
    }

    private void increaseVTimestamp()
    {
        int currentTime = vectorTimestamp.get(processID);
        vectorTimestamp.put(processID, currentTime + 1);
    }
    
//*********************
//*                   *
//*    RMI METHODS    *
//*                   *
//*********************
    
    @Override
    public Map<Integer, Integer> getVTimestamp() throws RemoteException {
        return vectorTimestamp;
    }

    @Override
    public void setTimestamp(Map<Integer, Integer> vectorTimestamp) throws RemoteException 
    {
        for (int process : vectorTimestamp.keySet())
        {
            //Compare each processors time stamp
            Integer localProcessTime = this.vectorTimestamp.get(process);
            Integer remoteProcessTime = vectorTimestamp.get(process);
            if (localProcessTime == null ||
                    localProcessTime < remoteProcessTime)
            {
                this.vectorTimestamp.put(process, remoteProcessTime);
            }
        }
        this.vectorTimestamp = vectorTimestamp;
    }

    @Override
    public boolean takeSnapshot() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getBestLeader(int candidateID) throws RemoteException 
    { 
        electionInProgress = true; //TODO this will nullify all revotes so be careful.  Check manual.
        int electionLeader = candidateID;
        for (int candidate : clients.keySet())
        {
            if (candidate > electionLeader)
            {
                electionLeader = candidate;
            }
        }
        return electionLeader;
    }

    @Override
    public void setLeader(int leader) throws RemoteException 
    {
        electionInProgress = false;
        //check if self is leader
        if (leader == processID)
        {
            this.leader = this;
        } else
        {
            this.leader = clients.get(leader);
        }
    }

    @Override
    public RMIClient getLeader() throws RemoteException 
    {
        return leader;
    }
    
    @Override
    public int getProcessID() throws RemoteException 
    {
        return processID;
    }
    
    @Override
    public Map<Integer, RMIClient>  getClients() throws RemoteException 
    {
        return clients;
    }
    
    @Override
    public void updateClients(Map<Integer, RMIClient> clients) throws RemoteException {
        this.clients.putAll(clients);
    }

    @Override
    public List<String> getFileList() throws RemoteException {
        return fileList;
    }
    
    @Override
    public void updateFileList( List<String> fileList) throws RemoteException {
        this.fileList = fileList;
    }
}
