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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the server class.
 * 
*/
public class RMIServer implements RMIServerInterface
{

    // Server Registery Info
    public static final String REGISTRY_IDENTIFIER = "RMIServer";
    public static final int RMI_PORT = 8891;
    private RMIServerInterface localServerStub;
    private Registry localRegistry;
    private Registry remoteRegistry;
    private int regEntry;
    // Server Details
    private int processID;
    private Map<Integer, Integer> vectorTimestamp;
    private Map<Integer, RMIServerInterface> servers;
    private RMIServerInterface leader;
    // Directory for shared files
    private String filesDirectory = "files";
    private boolean serverRunning;
    private boolean electionInProgress;
    // GUI Status Infomation
    private String serverStatus = "stopped";
    private String electionStatus = "notstarted";
    //Christian Algorithm
    Timer clock;
    private long currentTime;
    //Listener
    private UserInterface listener;

    private boolean snapshotTaken;
    Map<Integer, Snapshot> systemSnapshot;

    public RMIServer()
    {
        this.vectorTimestamp = new HashMap<>();
        this.systemSnapshot = new HashMap<>();
        this.servers = new HashMap<>();
    }

    //*******************************
    //*                             *
    //*    Server Methods           *
    //*                             *
    //*******************************
    /**
     * Starts the server and sets up the RMI connections.
     *
     * @param leader
     */
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
                    System.out.println("Registry entry " + regEntry + " already in use.");
                    regEntry++;
                    continue;
                }
                nameInUse = false;
            }

            System.out.println("Created local RMI Entry (" + regEntry + ") on port " + RMI_PORT + ".");

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
            // Initlise vectorTimestamp
            this.vectorTimestamp.put(processID, 0);
        } else
        {
            try
            {
                // Get list of other servers from leader

                this.servers = leader.getServers(vectorTimestamp);

                // Add leader to list of servers
                this.servers.put(leader.getProcessID(vectorTimestamp), leader);
                System.out.println("Connected to " + servers.size() + " servers.");

                // Get ProcessID
                this.processID = leader.getNextProcessID(vectorTimestamp);
                System.out.println("Client was given Process ID #" + this.processID);

                // Initlise vectorTimestamp to 3 beacuse of three previous messages
                this.vectorTimestamp.put(processID, 3);

                // Connect to all other servers
                for (RMIServerInterface server : servers.values())
                {
                    increaseTimestamp();
                    server.addServer(this.processID, this, vectorTimestamp);
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
        initializeClock();
    }

    /**
     * Connects to a specified Server address via RMI.
     *
     * @param address
     */
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

    /**
     * Stops the server.
     */
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
                    increaseTimestamp();
                    server.removeServer(processID, vectorTimestamp);
                } catch (RemoteException ex)
                {
                    System.err.println("Error disconnecting from server:" + ex);
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
                System.err.println("Unable to unregister from the RMI Registry: " + ex);
            }

            // Reset server list
            servers.clear();

            // Stop Clock
            clock.cancel();

            System.out.println("Disconnected from network.");
            // GUI Status
            this.serverStatus = "stopped";
            this.serverRunning = false;
        }
    }

    //*******************************
    //*                             *
    //*    RMI Server Methods       *
    //*                             *
    //*******************************
    @Override
    public int getProcessID(Map<Integer, Integer> timestamp) throws RemoteException
    {
        // Set vector timestamps 
        setTimestamp(timestamp);

        return processID;
    }

    @Override
    public synchronized int getNextProcessID(Map<Integer, Integer> timestamp) throws RemoteException
    {
        // Set vector timestamps 
        setTimestamp(timestamp);

        int highestProcessID = processID;
        for (int serverProcessID : servers.keySet())
        {
            if (serverProcessID > highestProcessID)
            {
                highestProcessID = serverProcessID;
            }
        }

        System.out.println("Assigned Process ID " + (highestProcessID + 1) + " by leader.");
        return highestProcessID + 1;
    }

    @Override
    public Map<Integer, RMIServerInterface> getServers(Map<Integer, Integer> timestamp) throws RemoteException
    {
        // Set vector timestamps 
        setTimestamp(timestamp);

        return servers;
    }

    @Override
    public void addServer(int processID, RMIServerInterface server, Map<Integer, Integer> timestamp) throws RemoteException
    {
        // Set vector timestamps 
        setTimestamp(timestamp);

        servers.put(processID, server);
    }

    @Override
    public void removeServer(int processID, Map<Integer, Integer> timestamp) throws RemoteException
    {
        // Set vector timestamps 
        setTimestamp(timestamp);

        // Remove server from map
        servers.remove(processID);
        System.out.println("Server #" + processID + " has disconnected.");

        // If server is leader start election
        increaseTimestamp();
        if (leader.getProcessID(vectorTimestamp) == processID)
        {
            increaseTimestamp();
            this.startElection(vectorTimestamp);
        }
    }

    //*******************************
    //*                             *
    //*    Election Methods         *
    //*                             *
    //*******************************
    @Override
    public void startElection(Map<Integer, Integer> timestamp) throws RemoteException
    {
        // Set vector timestamps 
        setTimestamp(timestamp);

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
                        increaseTimestamp();
                        server.setLeader(electionLeaderID, vectorTimestamp);
                    } catch (RemoteException ex)
                    {
                        Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

                // Set own to leader
                increaseTimestamp();
                this.setLeader(electionLeaderID, vectorTimestamp);

            } else
            {
                // Send vote to each lower peer
                for (int candidateID : servers.keySet())
                {
                    if (candidateID < processID)
                    {
                        try
                        {
                            increaseTimestamp();
                            servers.get(candidateID).startElection(vectorTimestamp);
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
    public void setLeader(int leaderID, Map<Integer, Integer> timestamp) throws RemoteException
    {
        // Set vector timestamps 
        setTimestamp(timestamp);

        electionInProgress = false;
        //check if self is leader
        if (leaderID == processID)
        {
            this.leader = this;
        } else
        {
            this.leader = servers.get(leaderID);
        }

        System.out.println("Process " + leaderID + " has been selected as the leader.");
        // GUI Status
        this.electionStatus = "complete";
    }

    //*******************************
    //*                             *
    //* Cristian's Algorithm       *
    //*                             *
    //*******************************
    /**
     * Sets up the local clock and starts the timer.
     */
    private void initializeClock()
    {
        currentTime = System.currentTimeMillis();
        TimerTask tickTask = new TimerTask()
        {
            @Override
            public void run()
            {
                currentTime += 1000;
            }
        };
        clock = new Timer();
        clock.scheduleAtFixedRate(tickTask, 0, 1000);
    }

    /**
     * Synchronizes with the leader using Cristian's algorithm.
     */
    public void syncTime()
    {
        if (leader != this)
        {
            System.out.println("Starting time sync with leader.");
            System.out.println("Current time is: " + getTimeAsString());
            long currTime = 0;
            long totalTime = 0;
            int numberOfResults = 0;
            for (int i = 0; i < 20; i++)
            {
                try
                {
                    increaseTimestamp();
                    long startTime = System.currentTimeMillis();
                    currTime = leader.getTime(vectorTimestamp);
                    totalTime += System.currentTimeMillis() - startTime;
                    numberOfResults++;
                } catch (RemoteException ex)
                {
                }
            }
            if (numberOfResults == 0)
            {
                System.err.println("No reply from leader.  Starting election.");
                try
                {
                    increaseTimestamp();
                    startElection(vectorTimestamp); //Leader has disconnected (or self)
                } catch (RemoteException ex)
                {
                    Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else
            {
                currentTime = currTime + (totalTime / numberOfResults);
            }
            System.out.println("New time is: " + getTimeAsString(currTime));
        } else
        {
            currentTime = System.currentTimeMillis();
        }
    }

    @Override
    public long getTime(Map<Integer, Integer> timestamp) throws RemoteException
    {
        // Set vector timestamps 
        setTimestamp(timestamp);

        return currentTime;
    }

    //*******************************
    //*                             *
    //*    Timestamp Methods        *
    //*                             *
    //*******************************
    /**
     * Increases the local process timestamp by 1.
     */
    private void increaseTimestamp()
    {
        int currentTimestamp = vectorTimestamp.get(processID);
        vectorTimestamp.put(processID, currentTimestamp + 1);
    }

    @Override
    public Map<Integer, Integer> getVTimestamp(Map<Integer, Integer> timestamp) throws RemoteException
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
    }

    //*******************************
    //*                             *
    //*    Snapshot Methods         *
    //*                             *
    //*******************************
    @Override
    public Map<Integer, Snapshot> takeSnapshot(Map<Integer, Integer> timestamp) throws RemoteException
    {
        setTimestamp(timestamp);
        Map<Integer, Snapshot> neighbourSnapshots = new HashMap<>();
        if (!snapshotTaken)
        {
            snapshotTaken = true;
            neighbourSnapshots.put(processID, new Snapshot(getFileList(timestamp)));
            for (RMIServerInterface server : servers.values())
            {
                increaseTimestamp();
                try
                {
                    neighbourSnapshots.putAll(server.takeSnapshot(vectorTimestamp));
                } catch (RemoteException ex)
                {
                    System.out.println("Error taking snapshot: " + ex);
                }
            }
        }
        return neighbourSnapshots;
    }

    @Override
    public void snapshotFinished(Map<Integer, Integer> timestamp, Map<Integer, Snapshot> systemSnapshot)
            throws RemoteException
    {
        setTimestamp(timestamp);
        this.systemSnapshot = systemSnapshot;
        snapshotTaken = false;
        notifyListener("SNAPSHOT");
    }

    /**
     * Starts the process to take a system snapshot.
     */
    public void startSnapshot()
    {
        try
        {
            systemSnapshot = takeSnapshot(vectorTimestamp);
        } catch (RemoteException ex)
        {
            System.err.println("Error taking snapshot: " + ex);
        }
        for (RMIServerInterface server : servers.values())
        {
            increaseTimestamp();
            try
            {
                server.snapshotFinished(vectorTimestamp, systemSnapshot);
            } catch (RemoteException ex)
            {
                System.err.println("Error finishing snapshot: " + ex);
            }
        }
        snapshotTaken = false;
        notifyListener("SNAPSHOT");
    }

    //*******************************
    //*                             *
    //*    File Transfer Methods    *
    //*                             *
    //*******************************
    @Override
    public File[] getFileList(Map<Integer, Integer> timestamp) throws RemoteException
    {
        // Set vector timestamps 
        setTimestamp(timestamp);

        File folder = new File(filesDirectory);
        return folder.listFiles();

    }

    @Override
    public byte[] downloadFile(String fileName, Map<Integer, Integer> timestamp) throws RemoteException
    {
        // Set vector timestamps 
        setTimestamp(timestamp);

        try
        {
            File file = new File(filesDirectory + "/" + fileName);
            byte buffer[] = new byte[(int) file.length()];
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
            input.read(buffer, 0, buffer.length);
            input.close();
            return (buffer);
        } catch (Exception e)
        {
            System.err.println("Error downloading file: " + e.getMessage());
            return (null);
        }
    }

    /**
     * Gets and saves a file from the system leader.
     *
     * @param fileName
     */
    public void getFileFromLeader(String fileName)
    {
        try
        {
            increaseTimestamp();
            byte[] filedata = leader.downloadFile(fileName, vectorTimestamp);
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

    //*******************************
    //*                             *
    //*    GUI Infomation Methods   *
    //*                             *
    //*******************************
    /**
     * Gets a list of local files from the current leader.
     */
    public File[] getLeaderFileList()
    {
        try
        {
            increaseTimestamp();
            return leader.getFileList(vectorTimestamp);
        } catch (RemoteException ex)
        {
            System.err.println("Error getting files from leader: " + ex);
            return null;
        }
    }

    /**
     * Returns the local IP address of the PC.
     */
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

    /**
     * Returns the process ID of th client.
     */
    public int getLocalProcessID()
    {
        return this.processID;
    }

    /**
     * Returns the Process ID of the leader of the system.
     */
    public int getLeaderID()
    {
        int id = 0;
        try
        {
            increaseTimestamp();
            id = leader.getProcessID(vectorTimestamp);
        } catch (Exception ex)
        {
            // Ignore Exception if no leader yet
        }
        return id;
    }

    /**
     * Returns the number of servers currently connected in the system.
     */
    public int getNumServersConnected()
    {
        if (this.serverStatus.equals("running"))
        {
            return this.servers.size() + 1;
        }
        return this.servers.size();

    }

    /**
     * Gets the current status of the server.
     */
    public String getServerStatus()
    {
        return this.serverStatus;
    }

    /**
     * Starts an election from the interface.
     */
    public void forceElection()
    {
        try
        {
            increaseTimestamp();
            this.startElection(vectorTimestamp);
        } catch (RemoteException ex)
        {
            Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets the current state of any running election.
     */
    public String getElectionStatus()
    {
        return this.electionStatus;
    }

    /**
     * Returns the latest snapshot of the system.
     *
     * @return
     */
    public Map<Integer, Snapshot> getSystemSnapshot()
    {
        return systemSnapshot;
    }

    /**
     * Sets the interface listener for notifications.
     *
     * @param listener
     */
    public void setListener(UserInterface listener)
    {
        this.listener = listener;
    }

    /**
     * Sends a notification to the interface listener.
     */
    private void notifyListener(String notice)
    {
        listener.notifyChange(notice);
    }

    //*******************************
//*                             *
//*    Convenience Methods      *
//*                             *
//*******************************
    /**
     * Converts the current time into a human readable format.
     */
    public String getTimeAsString()
    {
        Date date = new Date(currentTime);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(date);
    }

    /**
     * Converts a specified time into a human readable format.
     */
    private String getTimeAsString(long currTime)
    {
        Date date = new Date(currTime);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(date);
    }

}
