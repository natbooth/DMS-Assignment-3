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
public class Main
{
    
    public static final String REGISTRY_URL = "192.168.1.20";
    public static final String REGISTRY_IDENTIFIER = "backupclient"; //need multiple
    public static final int RMI_PORT = 8891;
    
    public static void main(String[] args)
    {
        
        final RMIServer server = new RMIServer();
        try
        {

            //set up a stub so remote clients can interact with this client
            RMIServerInterface serverStub = (RMIServerInterface) UnicastRemoteObject.exportObject(server, 0);
            //Setup the registry
            try
            {
                LocateRegistry.createRegistry(RMI_PORT);
            } catch (RemoteException e)
            {
                //already running
                System.out.println("Server already running on port " + RMI_PORT + ".");
            }            
            Registry localRegistry = LocateRegistry.getRegistry("localhost", RMI_PORT);
            //Find an unused registry name (for multiple clients on one machine)
            boolean nameInUse = true;
            int regEntry = 0;
            while (nameInUse)
            {
                try
                {
                    localRegistry.bind(REGISTRY_IDENTIFIER + regEntry, serverStub);
                } catch (AlreadyBoundException ex)
                {
                    System.out.println("Registry entry " + regEntry + "already in use.");
                    regEntry++;
                    continue;
                }
                nameInUse = false;
            }
            System.out.println("Created local RMI Registry(" + regEntry
                    + ") on port " + RMI_PORT + ".");
            //On ui action, connect to server using a provided URL
            Scanner keyboardInput = new Scanner(java.lang.System.in);
            
            java.lang.System.out.print("Enter IP address of process in system (or enter for first process): ");
            String address = keyboardInput.nextLine().trim();
            
            if (address == null || address.length() == 0)
            {
                java.lang.System.out.println("Starting as first process in system");
                // start and run server in separate thread
                server.startServer(server);
            } else
            {
                java.lang.System.out.println("Connecting to existing system");
                Registry remoteRegistry = LocateRegistry.getRegistry(address, RMI_PORT);
                RMIServerInterface remoteProxy = (RMIServerInterface) remoteRegistry.lookup(REGISTRY_IDENTIFIER + "0");
                System.out.println("Connected to Server at " + REGISTRY_URL + ":" + RMI_PORT + ".");
                server.startServer(remoteProxy);
    //            server.connect(address);

                //            //pass clients to server?
                ////            server.setClients(clients);
                //            //also set leader
            }
            boolean stopRequested = false;

            // use keyboard input for requesting access to critical section
            System.out.println("Type \"exit\" to abort)");
            while (!stopRequested)
            {
                String line = keyboardInput.nextLine();
                if ("exit".equalsIgnoreCase(line.trim()))
                {
                    stopRequested = true;
                    server.stopServer();
                } else
                {
                    server.broadcastMessage(line);
                }
            }
            java.lang.System.out.println("Exiting process");
            localRegistry.unbind(REGISTRY_IDENTIFIER + regEntry);
           
        } catch (RemoteException e)
        {            
            System.err.println("Unable to use registry: " + e);
        } catch (NotBoundException e)
        {
            System.err.println("Registry entry '" + REGISTRY_IDENTIFIER + "0' generated an error: " + e);
        }   
        System.exit(0);     
    }
}
