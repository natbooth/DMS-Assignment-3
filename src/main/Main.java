package main;

import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nat Booth
 */
public class Main {

    public static final String REGISTRY_URL = "localhost";
    public static final String REGISTRY_IDENTIFIER = "backupclient"; //need multiple
    public static final int RMI_PORT = 8891;
    
    public static void main(String[] args) {

        final Server server = new Server();
        try
        {  
            //setup security manager to download remote classes
            if (System.getSecurityManager() == null) 
            {
                System.setSecurityManager(new SecurityManager());
            }
            //Setup the registry
            LocateRegistry.createRegistry(RMI_PORT);
            Registry registry = LocateRegistry.getRegistry(REGISTRY_URL);
            //set up a stub so remote clients can interact with this client
            RMIClient clientStub = (RMIClient) UnicastRemoteObject.exportObject(server, RMI_PORT);
            registry.rebind(REGISTRY_IDENTIFIER, clientStub);
            //Get reference to remote clients?
            Map<Integer, RMIClient> clients = new HashMap<>();
            try
            {  
                String[] bindings = Naming.list(REGISTRY_URL);
                for (String name : bindings)
                {
                   if (name.contains(REGISTRY_IDENTIFIER))
                   {
                       RMIClient client = (RMIClient) registry.lookup(name);
                       clients.put(client.getProcessID(), client);
                   }
                }
            }
            catch (MalformedURLException e)
            {  
                System.err.println("Unable to get values in registry: " + e);
            }     
            //pass clients to server?
            server.setClients(clients);
            //also set leader
        }
        catch (RemoteException e)
        {  System.err.println("Unable to use registry: " + e);
        } catch (NotBoundException e) {
            System.err.println("Registry entry '" + REGISTRY_IDENTIFIER + "' generated an error: " + e);
        } 
        

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
