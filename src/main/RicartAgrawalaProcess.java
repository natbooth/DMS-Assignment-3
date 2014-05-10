package main;


/**
 * A class that demonstrates the Ricart-Agrawala algorithm for distributed
 * mutual exclusion, representing a single process in a distributed system. Each
 * process in the system communicates via messages that are either:
 * <UL>
 * <LI> request (to request access from all other processes to enter the
 * critical section),
 * <LI> okay (to grant a request from another processor),
 * <LI> timestamp (which is used to update this processes timestamp),
 * <LI> join (which is sent from a process to a new process and holds the IP
 * address of another process in the system).
 * </UL>
 * All messages except join include a timestamp of when the message was sent so
 * that the receiving process can update its clock
 *
 * @author Andrew Ensor
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;//synchronized queue

public class RicartAgrawalaProcess {

    private Queue<ProcessConnection> queue; // request queue
    private List<ProcessConnection> connections; // all other processes
    private String ownAddress;
    private int timestamp; // current Lamport timestamp
    private int pendingReplies; //no. replies pending before cs allowed
    private boolean ownRequest; //whether this process has requested cs
    private int ownRequestTimestamp; // used when ownRequest
    private boolean inCriticalSection; // whether currently in cs
    private boolean stopRequested;
    public static final int PORT = 8891; // some unused port number
    public static final String REQUEST = "request";
    public static final String OKAY = "okay";
    public static final String TIMESTAMP = "timestamp";
    public static final String JOIN = "join";

    public RicartAgrawalaProcess() {
        queue = new ConcurrentLinkedQueue<ProcessConnection>();
        connections = new ArrayList<ProcessConnection>();
        try {
            ownAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            System.out.println("Unable to get local host: " + e);
        }
        timestamp = 0;
        pendingReplies = 0;
        ownRequest = false;
        ownRequestTimestamp = 0;
        inCriticalSection = false;
        stopRequested = false;
    }

   // start the server if not already started and repeatedly listen
    // for client connections until stop requested
    public void startServer() {
        stopRequested = false;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            serverSocket.setSoTimeout(1000); // timeout for accept
            System.out.println("Server started at " + ownAddress
                    + " on port " + PORT);
        } catch (IOException e) {
            System.err.println("Server can't listen on port: " + e);
            System.exit(-1);
        }
        while (!stopRequested) {  // block until the next client requests a connection
            // or else the server socket timeout is reached
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Server connection made with "
                        + socket.getInetAddress().getHostAddress());
                // start a connection with this socket
                ProcessConnection connection = new ProcessConnection(socket);
            // send the new client process a series of join messages
                // so that it can connect to other processes in system
                for (ProcessConnection conn : connections) {
                    connection.sendMessage(JOIN + " " + conn.getAddress());
                }
                synchronized (connections) {
                    connections.add(connection);
                }
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

   // create a client connection with specified IP address where there
    // should be a running server process
    public void connect(String address) {
        try {
            Socket socket = new Socket(address, PORT);
            System.out.println("Client connection made with "
                    + socket.getInetAddress().getHostAddress());
            // start a connection with this socket
            ProcessConnection connection = new ProcessConnection(socket);
            synchronized (connections) {
                connections.add(connection);
            }
            Thread thread = new Thread(connection);
            thread.start();
        } catch (IOException e) {
            System.out.println("Unable to open client connection: " + e);
        }
    }

    public void receiveMessage(String message,
            ProcessConnection connection) {
        if (message.startsWith(REQUEST)) {  // get the timestamp with message
            try {
                int otherTimestamp = Integer.parseInt(message.substring(REQUEST.length()).trim());
                // check the three cases
                if (inCriticalSection) {
                    queue.offer(connection);
                } else if (!ownRequest) {
                    connection.sendMessage(OKAY + " " + getTime());
                } else {  // this process is also waiting to enter the critical
                    // section, determine which process goes first
                    if ((otherTimestamp < ownRequestTimestamp)
                            || ((otherTimestamp == ownRequestTimestamp)
                            && (connection.getAddress().compareTo(ownAddress) < 0))) {
                        connection.sendMessage(OKAY + " " + getTime());
                    } else {
                        queue.offer(connection);
                    }
                }
                updateTime(otherTimestamp + 1); // one tick for transmission
            } catch (NumberFormatException e) {
                System.out.println("Could not extract timestamp from message: " + e);
            }
        } else if (message.startsWith(OKAY)) {  // get the timestamp with message
            try {
                int otherTimestamp = Integer.parseInt(message.substring(OKAY.length()).trim());
                updateTime(otherTimestamp + 1); // one tick for transmission
            } catch (NumberFormatException e) {
                System.out.println("Could not extract timestamp from message: " + e);
            }
            // decrease the number of pending replies
            synchronized (connections) {
                pendingReplies--;
            }
            if (pendingReplies == 0) {  // can enter critical section
                System.out.println("Entering critical section");
                inCriticalSection = true;
                try {
                    Thread.sleep(3000); //simulate time in critical section
                } catch (InterruptedException e) {  // ignore
                }
                System.out.println("Exiting critical section");
                inCriticalSection = false;
                exitCriticalSection();
            }
        } else if (message.startsWith(TIMESTAMP)) {  // get the timestamp with message
            try {
                int otherTimestamp = Integer.parseInt(message.substring(TIMESTAMP.length()).trim());
                updateTime(otherTimestamp + 1); // one tick for transmission
            } catch (NumberFormatException e) {
                System.out.println("Could not extract timestamp from message: " + e);
            }
        } else if (message.startsWith(JOIN)) {  // get the IP address included with the join message
            String address = message.substring(JOIN.length()).trim();
         // check whether there is already a connection with this
            // IP address, either this connection or another in list
            boolean alreadyConnected = connection.getAddress().equals(address);
            synchronized (connections) {
                for (ProcessConnection conn : connections) {
                    if (conn.getAddress().equals(address)) {
                        alreadyConnected = true;
                    }
                }
            }
            if (!alreadyConnected) {  // make a client connection to that IP address
                connect(address);
            }
        } else {
            System.out.println("Unknown type of message received: "
                    + message);
        }
    }

    public void requestCriticalSection() {
        if (!ownRequest) {
            ownRequest = true;
            ownRequestTimestamp = timestamp;
            synchronized (connections) {  // send request to all other processes
                pendingReplies = 0;
                for (ProcessConnection connection : connections) {
                    connection.sendMessage(REQUEST + " " + getTime());
                    pendingReplies++;
                }
            }
        }
    }

    public void exitCriticalSection() {
        ownRequest = false;
        while (queue.peek() != null) {
            ProcessConnection connection = queue.poll();
            connection.sendMessage(OKAY + " " + getTime());
        }
    }

   // stops server AFTER the next client connection has been made
    // or timeout is reached
    public void requestStop() {
        stopRequested = true;
    }

    private synchronized void incrementTime() {
        timestamp++;
    }

    private synchronized int getTime() {
        return timestamp;
    }

    private synchronized void updateTime(int otherTimestamp) {
        timestamp = Math.max(timestamp, otherTimestamp);
    }

    public static void main(String[] args) {
        final RicartAgrawalaProcess process = new RicartAgrawalaProcess();
        Scanner keyboardInput = new Scanner(System.in);
        System.out.print("Enter IP address of process in system "
                + " (or enter for first process): ");
        String address = keyboardInput.nextLine().trim();
        if (address == null || address.length() == 0) {
            System.out.println("Starting as first process in system");
        } else {
            System.out.println("Connecting to existing system");
            process.connect(address);
        }
        // start and run server in separate thread
        Thread thread = new Thread(new Runnable() {
            public void run() {
                process.startServer();
            }
        });
        thread.start();
        // use keyboard input for requesting access to critical section
        System.out.println("Press enter to enter critical section "
                + "(or done to abort)");
        while (!process.stopRequested) {
            String line = keyboardInput.nextLine();
            if ("done".equalsIgnoreCase(line.trim())) {
                process.stopRequested = true;
            } else {
                process.requestCriticalSection();
            }
        }
        System.out.println("Exiting process");
    }

   // inner class that represents a connection with one other process
    // in the distributed system
    private class ProcessConnection implements Runnable {

        private Socket socket;
        private PrintWriter pw;
        private BufferedReader br;

        public ProcessConnection(Socket socket) {
            this.socket = socket;
            try {
                socket.setSoTimeout(500); // 0.5 second timeout
                // create an autoflush output stream for the socket
                pw = new PrintWriter(socket.getOutputStream(), true);
                // create a buffered input stream for this socket
                br = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
            } catch (SocketException e) {
                System.out.println("Unable to set socket timeout: " + e);
            } catch (IOException e) {
                System.out.println("Unable to open streams: " + e);
            }
        }

        public void sendMessage(String message) {
            System.out.println("Sending message: " + message
                    + "; Destination: " + getAddress());
            pw.println(message);
            incrementTime();
        }

        public String getAddress() {
            return socket.getInetAddress().getHostAddress();
        }

        public void run() {
            try {  // listen for messages until stopRequested
                String clientRequest;
                do {  // wait for message or until timeout is reached
                    try {
                        final String message = br.readLine().trim(); //block
                        incrementTime();
                        // then pass message to process to handle
                        Thread thread = new Thread(new Runnable() {
                            public void run() {
                                System.out.println("Received message: "
                                        + message + "; Sender: " + getAddress());
                                receiveMessage(message,
                                        ProcessConnection.this);
                            }
                        });
                        thread.start();
                    } catch (SocketTimeoutException e) {  // ignore and try again unless stopRequested
                    }
                } while (!stopRequested);
                System.out.println("Closing connection with "
                        + socket.getInetAddress());
            } catch (IOException e) {
                System.err.println("Server error: " + e);
            } finally {
                try {
                    if (pw != null) {
                        pw.close();
                    }
                    if (br != null) {
                        br.close();
                    }
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Failed to close streams: " + e);
                }
            }
        }
    }
}
