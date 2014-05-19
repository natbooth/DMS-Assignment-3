package main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the server class. TODO call election start on disconnect.
 */
public class RMIServer implements RMIServerInterface
{

    // Server Registery Info
    public static final String REGISTRY_IDENTIFIER = "RMIServer";
    public static final int RMI_PORT = 8891;

    RMIServerInterface localServerStub;
    private Registry localRegistry;
    private Registry remoteRegistry;
    private int regEntry;
    private Map<Integer, Integer> vectorTimestamp;
    private int processID;
    private int timestamp;
    private Map<Integer, RMIServerInterface> servers;
    private RMIServerInterface leader;
    private String filesDirectory = "files";
    private boolean serverRunning;
    private boolean electionInProgress;
    // GUI Status Infomation
    private String serverStatus = "stopped";
    private String electionStatus = "notstarted";

    public RMIServer()
    {
        this.timestamp = 0;
        this.vectorTimestamp = new HashMap<>();
        this.servers = new HashMap<>();
    }

    public void setServers(Map<Integer, RMIServerInterface> servers)
    {
        //remove self
        servers.remove(processID);
        this.servers = servers;
    }

    public void setProcessID(int processID)
    {
        this.processID = processID;
    }

    public int getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(int timestamp)
    {
        this.timestamp = timestamp;
    }

    // Start the server
    public void startServer(RMIServerInterface leader)
    {
        // GUI Status
        this.serverStatus = "starting";
        try
        {
            // Set up a stub so remote clients can interact with this server
            localServerStub = (RMIServerInterface) UnicastRemoteObject.exportObject(this, 0);

            // Setup the local registry
            try
            {
                LocateRegistry.createRegistry(RMI_PORT);
            } catch (RemoteException e)
            {
                // Already running
                System.out.println("Server already running on port " + RMI_PORT + ".");
            }

            localRegistry = LocateRegistry.getRegistry("localhost", RMI_PORT);

            // Find an unused registry name (for multiple clients on one machine)
            boolean nameInUse = true;
            regEntry = 0;
            while (nameInUse)
            {
                try
                {
                    localRegistry.bind(REGISTRY_IDENTIFIER + regEntry, localServerStub);
                } catch (AlreadyBoundException ex)
                {
                    System.out.println("Registry entry " + regEntry + "already in use.");
                    regEntry++;
                    continue;
                }
                nameInUse = false;
            }

            System.out.println("Created local RMI Registry(" + regEntry + ") on port " + RMI_PORT + ".");

        } catch (RemoteException e)
        {
            System.err.println("Unable to use registry: " + e);
        }

        // Set Coordinator
        this.leader = leader;

        // Set processID
        if (this == leader)
        {
            this.processID = 1;
        } else
        {
            try
            {
                // Get list of other servers from leader
                this.servers = leader.getServers();

                // Add leader to list of servers
                this.servers.put(leader.getProcessID(), leader);
                System.out.println("Connected to " + servers.size() + " servers.");

                // Get ProcessID
                this.processID = leader.getNextProcessID();
                System.out.println("Client was given Process ID #" + this.processID);

                // Connect to all other servers
                for (RMIServerInterface server : servers.values())
                {
                    server.addServer(this.processID, this);
                }
            } catch (RemoteException ex)
            {
                Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.vectorTimestamp.put(processID, 0);
        System.out.println("Server setup complete.");

        // GUI Status
        this.serverStatus = "running";
        this.electionStatus = "complete";
        this.serverRunning = true;
    }

    public void connect(String address)
    {

        try
        {
            // Connect to leader at specified address
            remoteRegistry = LocateRegistry.getRegistry(address, RMI_PORT);
            RMIServerInterface remoteServerStub = (RMIServerInterface) remoteRegistry.lookup(REGISTRY_IDENTIFIER + "0");
            System.out.println("Connected to Server at " + address + ":" + RMI_PORT + ".");
            // Start server with leader
            this.startServer(remoteServerStub);
        } catch (RemoteException | NotBoundException ex)
        {
            Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Stops server AFTER the next client connection has been made or timeout is reached
    public void stopServer()
    {
        if (serverRunning)
        {
            // GUI Status
            this.serverStatus = "stopping";

            // Disconnect from each server
            for (RMIServerInterface server : servers.values())
            {
                try
                {
                    server.removeServer(processID);
                } catch (Exception ex)
                {
                    Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            // Wait for disconnect message to be recived.
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException ex)
            {
                Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
            }

            // TODO fix error when shutting down on server that was not orignally a leader
            // Remove registery entry
            try
            {
                localRegistry.unbind(REGISTRY_IDENTIFIER + regEntry);
                UnicastRemoteObject.unexportObject(this, false);
            } catch (RemoteException | NotBoundException ex)
            {
                Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Reset server list
            servers.clear();

            System.out.println("Disconnected from network.");
            // GUI Status
            this.serverStatus = "stopped";
            this.serverRunning = false;
        }
    }

    //*********************
    //*                   *
    //*    Election Methods    
    //*                   *
    //*********************
    @Override
    public void startElection() throws RemoteException
    {

        if (!electionInProgress)
        {
            // GUI Status
            electionStatus = "started";
            electionInProgress = true;
            System.out.println("Starting election.");

            // Nominate best leader starting with self
            int electionLeaderID = processID;

            for (int candidateID : servers.keySet())
            {
                if (candidateID < electionLeaderID)
                {
                    electionLeaderID = candidateID;
                }
            }

            // If electionLeader is self send election message to each server
            if (electionLeaderID == processID)
            {
                for (RMIServerInterface server : servers.values())
                {

                    try
                    {
                        server.setLeader(electionLeaderID);
                    } catch (RemoteException ex)
                    {
                        Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

                // Set own to leader
                this.setLeader(electionLeaderID);

            } else
            {
                // Send vote to each lower peer
                for (int candidateID : servers.keySet())
                {
                    if (candidateID < processID)
                    {
                        try
                        {
                            servers.get(candidateID).startElection();
                        } catch (RemoteException ex)
                        {
                            // Message not recived
                            //Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setLeader(int leaderID) throws RemoteException
    {
        electionInProgress = false;
        //check if self is leader
        if (leaderID == processID)
        {
            this.leader = this;
        } else
        {
            this.leader = servers.get(leaderID);
        }

        System.out.println("Process " + leaderID + " is selected leader");

        System.out.println("Election complete.");
        // GUI Status
        this.electionStatus = "complete";
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
    public Map<Integer, Integer> getVTimestamp() throws RemoteException
    {
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
            if (localProcessTime == null
                    || localProcessTime < remoteProcessTime)
            {
                this.vectorTimestamp.put(process, remoteProcessTime);
            }
        }
        this.vectorTimestamp = vectorTimestamp;
    }

    @Override
    public boolean takeSnapshot() throws RemoteException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getProcessID() throws RemoteException
    {
        System.out.println("ProcessID requested.");
        return processID;
    }

    @Override
    public synchronized int getNextProcessID() throws RemoteException
    {
        int highestProcessID = processID;
        for (int serverProcessID : servers.keySet())
        {
            if (serverProcessID > highestProcessID)
            {
                highestProcessID = serverProcessID;
            }
        }

        System.out.println("Assigning processID " + (highestProcessID + 1));
        return highestProcessID + 1;
    }

    @Override
    public Map<Integer, RMIServerInterface> getServers() throws RemoteException
    {
        return servers;
    }

    // TODO do we use this method?
    @Override
    public void updateServers(Map<Integer, RMIServerInterface> servers) throws RemoteException
    {
        this.servers.putAll(servers);
    }

    @Override
    public void addServer(int processID, RMIServerInterface server) throws RemoteException
    {
        servers.put(processID, server);
    }

    @Override
    public void removeServer(int processID) throws RemoteException
    {
        // Remove server from map
        servers.remove(processID);
        System.out.println("Removed server #" + processID);

        // If server is leader start election
        if (leader.getProcessID() == processID)
        {
            this.startElection();
        }
    }

    //*********************
    //*                   *
    //* File Transfer Methods
    //*                   *
    //*********************
    @Override
    public File[] getFileList() throws RemoteException
    {
        System.out.println("File List Requested");
        File folder = new File(filesDirectory);
        return folder.listFiles();

    }

    @Override
    public byte[] downloadFile(String fileName) throws RemoteException
    {
        try
        {
            File file = new File(filesDirectory + "/" + fileName);
            byte buffer[] = new byte[(int) file.length()];
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(filesDirectory + "/" + fileName));
            input.read(buffer, 0, buffer.length);
            input.close();
            return (buffer);
        } catch (Exception e)
        {
            System.out.println("downloadFile: " + e.getMessage());
            e.printStackTrace();
            return (null);
        }
    }

    public void getFileFromLeader(String fileName)
    {
        try
        {
            byte[] filedata = leader.downloadFile(fileName);
            File file = new File(filesDirectory + "/" + fileName);
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(filesDirectory + "/" + fileName));
            output.write(filedata, 0, filedata.length);
            output.flush();
            output.close();

        } catch (RemoteException ex)
        {
            Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex)
        {
            Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //*********************
    //*                   *
    //* GUI Infomation Methods
    //*                   *
    //*********************
    public File[] getLeaderFileList()
    {
        try
        {
            return leader.getFileList();
        } catch (RemoteException ex)
        {
            System.out.println("getLeaderFileList: " + ex);
            return null;
        }
    }

    public String getLocalIPAddress()
    {
        String address = "";
        try
        {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex)
        {
            Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return address;
    }

    public int getLocalProcessID()
    {
        return this.processID;
    }

    public int getLeaderID()
    {
        int id = 0;
        try
        {
            id = leader.getProcessID();
        } catch (Exception ex)
        {
            // Ignore Exception if no leader yet
        }
        return id;
    }

    public int getNumServersConnected()
    {
        if (this.serverStatus.equals("running"))
        {
            return this.servers.size() + 1;
        }
        return this.servers.size();

    }

    public String getServerStatus()
    {
        return this.serverStatus;
    }

    public String getElectionStatus()
    {
        return this.electionStatus;
    }

}
