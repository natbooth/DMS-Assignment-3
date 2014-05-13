package main;

import java.util.Scanner;

/**
 *
 * @author Nat Booth
 */
public class Main {

    public static void main(String[] args) {

        final Server server = new Server();

        Scanner keyboardInput = new Scanner(java.lang.System.in);

        java.lang.System.out.print("Enter IP address of process in system (or enter for first process): ");
        String address = keyboardInput.nextLine().trim();

        if (address == null || address.length() == 0) {
            java.lang.System.out.println("Starting as first process in system");
            // start and run server in separate thread
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    server.startServer(true);
                }
            });
            thread.start();
        } else {
            java.lang.System.out.println("Connecting to existing system");
            // start and run server in separate thread
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    server.startServer(false);
                }
            });
            thread.start();
            server.connect(address);
        }

        boolean stopRequested = false;

        // use keyboard input for requesting access to critical section
        java.lang.System.out.println("Type \"exit\" to abort)");
        while (!stopRequested) {
            String line = keyboardInput.nextLine();
            if ("exit".equalsIgnoreCase(line.trim())) {
                stopRequested = true;
                server.stopServer();
            } else if ("disconnect".equalsIgnoreCase(line.trim())) {
                server.disconnect(null);
            } else {
                server.broadcastMessage(line);
            }
        }
        java.lang.System.out.println("Exiting process");
    }
}
