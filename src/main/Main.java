package main;

import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
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

    public static final String REGISTRY_URL = "192.168.1.20";
    public static final String REGISTRY_IDENTIFIER = "backupclient"; //need multiple
    public static final int RMI_PORT = 8891;
    
    public static void main(String[] args) {

        final Server server = new Server();
        try
        {  
            
            //set up a stub so remote clients can interact with this client
            RMIClient clientStub = (RMIClient) UnicastRemoteObject.exportObject(server, 0);
            //Setup the registry
            try
            {
                LocateRegistry.createRegistry(RMI_PORT);
            } catch (RemoteException e) {
                //already running
                System.out.println("Server already running on port " + RMI_PORT + ".");
            } 
            Registry registry = LocateRegistry.getRegistry("localhost", RMI_PORT);
            //Find an unused registry name (for multiple clients on one machine)
            boolean nameInUse = true;
            int regEntry = 0;
            while (nameInUse)
            {
                try {
                    registry.bind(REGISTRY_IDENTIFIER + regEntry, clientStub);
                } catch (AlreadyBoundException ex) {
                    System.out.println("Registry entry " + regEntry + "already in use.");
                    regEntry++;
                    continue;
                }
                nameInUse = false;
            }
            System.out.println("Created local RMI Registry(" + regEntry +
                    ") on port " + RMI_PORT + ".");
            
            //Setup the registry
            //Get reference to remote clients?
            //Totally pointless, impossible for your registry to have any clients in it.
//            Map<Integer, RMIClient> clients = new HashMap<>();
//            try
//            {  
//                String[] bindings = Naming.list("rmi://localhost:" + RMI_PORT);
//                for (String name : bindings)
//                {
//                   if (name.contains(REGISTRY_IDENTIFIER))
//                   {
//                       System.out.println("Found client " + name + " in registry.");
////                       RMIClient client = (RMIClient) registry.lookup(name);
////                       clients.put(client.getProcessID(), client);
//                   }
//                }
//            }
//            catch (MalformedURLException e)
//            {  
//                System.err.println("Unable to get values in registry: " + e);
//            }     
//            System.out.println("Found " + clients.size() + " remote clients.");
            //On ui action, connect to server using a provided URL
            
            Scanner keyboardInput = new Scanner(java.lang.System.in);

            java.lang.System.out.print("Enter IP address of process in system (or enter for first process): ");
            String address = keyboardInput.nextLine().trim();

            if (address == null || address.length() == 0) {
                java.lang.System.out.println("Starting as first process in system");
                // start and run server in separate thread
                server.initializeServer(server);
                server.start();
            } else {
                java.lang.System.out.println("Connecting to existing system");
                registry = LocateRegistry.getRegistry(address, RMI_PORT);
                RMIClient remoteProxy = (RMIClient) registry.lookup(REGISTRY_IDENTIFIER + "0");
                System.out.println("Connected to Server at " + REGISTRY_URL + ":" + RMI_PORT + ".");
                server.initializeServer(remoteProxy);
                server.start();
    //            server.connect(address);
                
    //            //pass clients to server?
    ////            server.setClients(clients);
    //            //also set leader
            }
             boolean stopRequested = false;

        // use keyboard input for requesting access to critical section
        System.out.println("Type \"exit\" to abort)");
        while (!stopRequested) {
            String line = keyboardInput.nextLine();
            if ("exit".equalsIgnoreCase(line.trim())) {
                stopRequested = true;
                server.stopServer();
            } else {
                server.broadcastMessage(line);
            }
        }
        java.lang.System.out.println("Exiting process");
        }
        catch (RemoteException e)
        {  
            System.err.println("Unable to use registry: " + e);
        } catch (NotBoundException e) {
            System.err.println("Registry entry '" + REGISTRY_IDENTIFIER + "0' generated an error: " + e);
        } 
    }
}
