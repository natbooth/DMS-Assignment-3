package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private boolean snapshotTaken;
    Map<Integer, Snapshot> systemSnapshot;
    

    public RMIServer()
    {
        this.vectorTimestamp = new HashMap<>();
        this.servers = new HashMap<>();
        initializeClock();
    }

    //*******************************
    //*                             *
    //*    Server Methods           *
    //*                             *
    //*******************************
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
    }

    // Connect to server
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

    // Stop server
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

        System.out.println("ProcessID requested.");
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

        System.out.println("Assigning processID " + (highestProcessID + 1));
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
        System.out.println("Removed server #" + processID);

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

        System.out.println("Process " + leaderID + " is selected leader");

        System.out.println("Election complete.");
        // GUI Status
        this.electionStatus = "complete";
    }

    //*******************************
    //*                             *
    //* Christian's Algorithm       *
    //*                             *
    //*******************************
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
                System.out.println("No reply from leader.  Starting election.");
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

        System.out.println("Request for time");
        return currentTime;
    }

    //*******************************
    //*                             *
    //*    Timestamp Methods         *
    //*                             *
    //*******************************
    private void increaseTimestamp()
    {

        int currentTimestamp = vectorTimestamp.get(processID);
        vectorTimestamp.put(processID, currentTimestamp + 1);
        System.out.println("Increaseing timestamp to " + (currentTimestamp + 1));
    }

    @Override
    public Map<Integer, Integer> getVTimestamp(Map<Integer, Integer> timestamp) throws RemoteException
    {
        return vectorTimestamp;
    }

    @Override
    public void setTimestamp(Map<Integer, Integer> vectorTimestamp) throws RemoteException
    {
        System.out.println("--- SET TIMESTAMP BEGIN ---");
        System.out.println("Setting timestamp from " + this.vectorTimestamp.toString() + " to " + vectorTimestamp.toString());
        for (int process : vectorTimestamp.keySet())
        {
            //Compare each processors time stamp
            Integer localProcessTime = this.vectorTimestamp.get(process);
            Integer remoteProcessTime = vectorTimestamp.get(process);
            if (localProcessTime == null
                    || localProcessTime < remoteProcessTime)
            {
                this.vectorTimestamp.put(process, remoteProcessTime);
                System.out.println("added timestamp {" + process + "->" + remoteProcessTime + "}.");
            }
        }
        System.out.println("New timestamp is " + this.vectorTimestamp.toString());
        System.out.println("--- SET TIMESTAMP END ---");
    }

    //*******************************
    //*                             *
    //*    Snapshot Methods         *
    //*                             *
    //*******************************
    @Override
    public Map<Integer, Snapshot> takeSnapshot(Map<Integer, Integer> timestamp) throws RemoteException
    {
        Map<Integer, Snapshot> neighbourSnapshots = new HashMap<>();
        if (!snapshotTaken)
        {
            neighbourSnapshots.put(processID, new Snapshot(getFileList(timestamp)));
            snapshotTaken = true;
            // Set vector timestamps 
            setTimestamp(timestamp);
            for (RMIServerInterface server : servers.values())
            {
                increaseTimestamp();
                try
                {
                    neighbourSnapshots.putAll(server.takeSnapshot(vectorTimestamp));
                } catch (RemoteException ex) { }
            }
        }
        return neighbourSnapshots;
    }

    @Override 
    public void snapshotFinished(Map<Integer, Integer> timestamp, Map<Integer, Snapshot> systemSnapshot) 
            throws RemoteException
    {
        setTimestamp(vectorTimestamp);
        this.systemSnapshot = systemSnapshot;
        snapshotTaken = false;        
    }
    
    public void startSnapshot()
    {
        try
        {
            systemSnapshot = takeSnapshot(vectorTimestamp);
        } catch (RemoteException ex) { }
        for (RMIServerInterface server : servers.values())
        {
            increaseTimestamp();
            try
            {
                server.snapshotFinished(vectorTimestamp, systemSnapshot);
            } catch (RemoteException ex){}
        }        
        snapshotTaken = false;
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

        System.out.println("File List Requested");
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
    public File[] getLeaderFileList()
    {
        try
        {
            increaseTimestamp();
            return leader.getFileList(vectorTimestamp);
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
            increaseTimestamp();
            id = leader.getProcessID(vectorTimestamp);
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

    public String getElectionStatus()
    {
        return this.electionStatus;
    }

    public Map<Integer, Snapshot> getSystemSnapshot()
    {
        return systemSnapshot;
    }
    
    
    //*******************************
//*                             *
//*    Convenience Methods      *
//*                             *
//*******************************
    public String getTimeAsString()
    {
        Date date = new Date(currentTime);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
        return formatter.format(date);
    }

    private String getTimeAsString(long currTime)
    {
        Date date = new Date(currTime);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
        return formatter.format(date);
    }

}
