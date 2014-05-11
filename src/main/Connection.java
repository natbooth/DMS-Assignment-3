package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 *
 * @author Nat
 */
public class Connection implements Runnable {

    //private int processID;
    private Server server;
    private String address;
    private Socket socket;
    private PrintWriter pw;
    private BufferedReader br;
    private boolean stopRequested;

    public Connection(Server server, Socket socket) {
        //this.processID = processID;
        this.server = server;
        this.socket = socket;
        this.address = socket.getInetAddress().getHostAddress();
        this.stopRequested = false;

        try {
            socket.setSoTimeout(500); // 0.5 second timeout
            // create an autoflush output stream for the socket
            pw = new PrintWriter(socket.getOutputStream(), true);
            // create a buffered input stream for this socket
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (SocketException e) {
            System.out.println("Unable to set socket timeout: " + e);
        } catch (IOException e) {
            System.out.println("Unable to open streams: " + e);
        }
    }

//    public int getProcessID() {
//        return this.processID;
//    }
    public String getAddress() {
        return this.address;
    }

    public void requestStop() {
        this.stopRequested = true;
    }

    public void sendMessage(String message) {
        System.out.println("Sending message: " + message + "; Destination: " + getAddress());
        pw.println(message);
    }

    public void run() {
        try {  // listen for messages until stopRequested
            do {  // wait for message or until timeout is reached
                try {
                    final String message = br.readLine().trim(); //block

                    // then pass message to process to handle
                    Thread thread = new Thread(new Runnable() {
                        public void run() {
                            System.out.println("Received message: " + message + "; Sender: " + getAddress());
                            server.receiveMessage(message, Connection.this);
                        }
                    });
                    thread.start();
                } catch (SocketTimeoutException e) {  // ignore and try again unless stopRequested
                }
            } while (!stopRequested);
            System.out.println("Closing connection with " + socket.getInetAddress());
        } catch (IOException e) {
            
            // Server Disconnected
            System.err.println("Server error: " + e);
            
            // Remove from connection list
            server.disconnect(Connection.this);
            System.err.println("Disconnected from " + address);

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
