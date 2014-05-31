/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 *
 * @author Tomas
 */
public interface RMIServerInterface extends Remote
{

// Server Methods
    /**
     * Gets the Process ID of a Server.
     */
    public int getProcessID(Map<Integer, Integer> timestamp) throws RemoteException;

    /**
     * Gets the next available Process ID from a server.
     */
    public int getNextProcessID(Map<Integer, Integer> timestamp) throws RemoteException;

    /**
     * Gets a list of all connected servers.
     */
    public Map<Integer, RMIServerInterface> getServers(Map<Integer, Integer> timestamp) throws RemoteException;

    /**
     * Adds a new server to the local list of servers.
     */
    public void addServer(int processID, RMIServerInterface server, Map<Integer, Integer> timestamp) throws RemoteException;

    /**
     * Removes a server from the local list of servers.
     */
    public void removeServer(int processID, Map<Integer, Integer> timestamp) throws RemoteException;

// Election Methods
    /**
     * Call to start an election.
     */
    public void startElection(Map<Integer, Integer> timestamp) throws RemoteException;

    /**
     * Sets the leader at the end of the election process.
     */
    public void setLeader(int leader, Map<Integer, Integer> timestamp) throws RemoteException;

// Cristian Algorithm
    /**
     * Gets the current time of the system.
     */
    public long getTime(Map<Integer, Integer> timestamp) throws RemoteException;

//Timestamps
    /**
     * Gets the list of timestamps on this server.
     */
    public Map<Integer, Integer> getVTimestamp(Map<Integer, Integer> timestamp) throws RemoteException;

    /**
     * Updates the timestamps on this server.
     */
    public void setTimestamp(Map<Integer, Integer> vectorTimestamp) throws RemoteException;

// ChandyLamport (Snapshots)
    /**
     * Takes a snapshot of the state of the system.
     */
    public Map<Integer, Snapshot> takeSnapshot(Map<Integer, Integer> timestamp) throws RemoteException;
    
    /**
     * Updates the local snapshot with the master list from the leader.
     */
    public void snapshotFinished(Map<Integer, Integer> timestamp, Map<Integer, Snapshot> systemSnapshto) throws RemoteException;
    
// File Transfer
    /**
     * Downloads the specified file from the leader.
     */
    public byte[] downloadFile(String fileName,  Map<Integer, Integer> timestamp) throws RemoteException;

    /**
     * Gets the list of files on the server.
     */
    public File[] getFileList(Map<Integer, Integer> timestamp) throws RemoteException;

}
