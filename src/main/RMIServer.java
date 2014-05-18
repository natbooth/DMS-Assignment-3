package main;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the server class. TODO call election start on disconnect.
 */
public class RMIServer implements RMIServerInterface
{

    private Map<Integer, Integer> timeStamp;
    private int processID;
    private Map<Integer, RMIServerInterface> servers;
    private RMIServerInterface leader;
    private boolean electionInProgress;
    
    public RMIServer()
    {
        this.timeStamp = new HashMap<>();
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
    
    // Start the server if not already started and repeatedly listen for client connections until stop requested
    public void startServer(RMIServerInterface leader)
    {

        // Set Coordinator
        this.leader = leader;

        System.out.println("Starting server thread.");

        // Set processID
        if (this == leader)
        {
            this.processID = 1;
            this.timeStamp.put(processID, 0);
        } else
        {
            try
            {
                this.servers = leader.getServers(timeStamp);
                this.servers.put(leader.getProcessID(timeStamp), leader);
                System.out.println("Connected to " + servers.size() + " servers.");
                this.processID = getNewProcessID();
                System.out.println("Client was given Process ID #" + this.processID);
                this.timeStamp.put(processID, 0);
                for (RMIServerInterface server : servers.values())
                {
                    server.addServer(this.processID, this, timeStamp);
                }
            } catch (RemoteException ex)
            {
                Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Server setup complete.");
    }

    public void stopServer()
    {
        //tell other clients that you are disconnecting
        for (RMIServerInterface server : servers.values())
        {
            try
            {
                server.removeServer(processID, timeStamp);
            } catch (RemoteException ex)
            {
                Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        System.out.println("Disconnected from network.");
    }
    
    private void startElection()
    {
        System.out.println("Starting election.");
        if (!electionInProgress)
        {
            //TODO remove the old leader from client list FIRST
            increaseVTimestamp();
            electionInProgress = true;
            int bestLeaderID = processID;
            //decide on own leader
            try
            {
                bestLeaderID = getBestLeader(processID, timeStamp);
            } catch (RemoteException ex){} //local, can't happen
            List<Integer> peerVotes = new ArrayList<>();
            //ask each client for their vote
            for (RMIServerInterface server : servers.values())
            {
                try
                {
                    increaseVTimestamp();
                    peerVotes.add(server.getBestLeader(bestLeaderID, timeStamp));
                    System.out.println("Peer voted for " + peerVotes.get(peerVotes.size() - 1));
                } catch (RemoteException e)
                {
                    System.err.println("Error connecting to server: " + e);
                }
            }
            bestLeaderID = processID;
            for (int i : peerVotes)
            {
                if (i < bestLeaderID)
                {
                    bestLeaderID = i;
                }
            }
            leader = servers.get(bestLeaderID);
            System.out.println("Leader selected is " + bestLeaderID);
            for (RMIServerInterface server : servers.values())
            {
                try
                {
                    increaseVTimestamp();
                    server.setLeader(bestLeaderID, timeStamp);
                } catch (RemoteException e)
                {
                    System.err.println("Error connecting to server: " + e);
                }
            }
            electionInProgress = false;
        }
    }

    public void broadcastMessage(String message)
    {

        switch (message.toLowerCase())
        {
            case "election":
                startElection();
                break;

            //debug commands
            case "processid":
                System.out.println("[D] ProcessID=" + processID);
                break;

            case "leader":
                int lead = -1;
                for (int i : servers.keySet())
                {
                    if (servers.get(i) == leader)
                    {
                        lead = i;
                        break;
                    }
                }
                if (lead > -1)
                {
                    System.out.println("[D] leader=" + lead);
                } else
                {
                    System.out.println("[D] leader=" + this);
                }
                break;
            
            case "timestamp" :
                System.out.println("Timestamp: " + timeStamp.toString());
                break;
                
            default:
                System.out.println("Command '" + message + "' not recognised.");
        }
    }
    
    private int getNewProcessID()
    {
        int highestProcessID = 0;
        for (int serverID : servers.keySet())
        {
            if (serverID > highestProcessID)
            {
                highestProcessID = serverID;
            }
        }

        return highestProcessID + 1;
    }

    private void increaseVTimestamp()
    {
        int currentTime = timeStamp.get(processID);
        timeStamp.put(processID, currentTime + 1);
    }

//*********************
//*                   *
//*    RMI METHODS    *
//*                   *
//*********************
    @Override
    public Map<Integer, Integer> getTimestamp(Map<Integer, Integer> timestamp) throws RemoteException
    {
        return timestamp;
    }

    @Override
    public void setTimestamp(Map<Integer, Integer> timestamp) throws RemoteException
    {
        System.out.println("--- SET TIMESTAMP BEGIN ---");
        System.out.println("Setting timestamp from " + this.timeStamp.toString() + " to " + timestamp.toString());
        for (int process : timestamp.keySet())
        {
            //Compare each processors time stamp
            Integer localProcessTime = this.timeStamp.get(process);
            Integer remoteProcessTime = timestamp.get(process);
            if (localProcessTime == null
                    || localProcessTime < remoteProcessTime)
            {
                this.timeStamp.put(process, remoteProcessTime);
                System.out.println("added timestamp {" + process + "->" + remoteProcessTime + "}.");
            }
        }
        System.out.println("New timestamp is " + this.timeStamp.toString());
        System.out.println("--- SET TIMESTAMP END ---");
    }

    @Override
    public boolean takeSnapshot(Map<Integer, Integer> timestamp) throws RemoteException
    {
        setTimestamp(timestamp);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getBestLeader(int candidateID, Map<Integer, Integer> timestamp) throws RemoteException
    {
        setTimestamp(timestamp);
        electionInProgress = true; //TODO this will nullify all revotes so be careful.  Check manual.
        int electionLeader = candidateID;
        for (int candidate : servers.keySet())
        {
            if (candidate < electionLeader)
            {
                electionLeader = candidate;
            }
        }
        System.out.println("Process " + processID + " is electing " + electionLeader);
        return electionLeader;
    }

    @Override
    public void setLeader(int leader, Map<Integer, Integer> timestamp) throws RemoteException
    {
        setTimestamp(timestamp);
        electionInProgress = false;
        //check if self is leader
        if (leader == processID)
        {
            this.leader = this;
        } else
        {
            this.leader = servers.get(leader);
        }
    }

    @Override
    public RMIServerInterface getLeader(Map<Integer, Integer> timestamp) throws RemoteException
    {
        setTimestamp(timestamp);
        return leader;
    }

    @Override
    public int getProcessID(Map<Integer, Integer> timestamp) throws RemoteException
    {
        setTimestamp(timestamp);
        System.out.println("ProcessID requested.");
        return processID;
    }

    @Override
    public Map<Integer, RMIServerInterface> getServers(Map<Integer, Integer> timestamp) throws RemoteException
    {
        setTimestamp(timestamp);
        return servers;
    }

    @Override
    public void updateServers(Map<Integer, RMIServerInterface> servers, Map<Integer, Integer> timestamp) throws RemoteException
    {
        setTimestamp(timestamp);
        this.servers = servers;
    }

    @Override
    public void addServer(int processID, RMIServerInterface server, Map<Integer, Integer> timestamp) throws RemoteException
    {
        setTimestamp(timestamp);
        servers.put(processID, server);
    }

    @Override
    public void removeServer(int processID, Map<Integer, Integer> timestamp) throws RemoteException
    {
        setTimestamp(timestamp);
        servers.remove(processID);
        System.out.println("Removed server #" + processID);
    }

    @Override
    public void sendFile(File file, Map<Integer, Integer> timestamp) throws RemoteException
    {
        setTimestamp(timestamp);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
