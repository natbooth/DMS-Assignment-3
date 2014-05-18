/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Tomas
 */
public interface RMIServerInterface extends Remote {
    
    //General methods
    public int getProcessID(Map<Integer, Integer> timestamp) throws RemoteException;
    public RMIServerInterface getLeader(Map<Integer, Integer> timestamp) throws RemoteException;
    public Map<Integer, RMIServerInterface> getServers(Map<Integer, Integer> timestamp) throws RemoteException;
    public void addServer(int processID, RMIServerInterface server, Map<Integer, Integer> timestamp) throws RemoteException;
    public void updateServers(Map<Integer, RMIServerInterface> servers, Map<Integer, Integer> timestamp) throws RemoteException;
    public void removeServer(int processID, Map<Integer, Integer> timestamp) throws RemoteException;
    //Cristian Algorithm (Time)
    public long getTime() throws RemoteException;
    //Timestamps
    public Map<Integer, Integer> getTimestamp(Map<Integer, Integer> timestamp) throws RemoteException;
    public void setTimestamp(Map<Integer, Integer> timestamp) throws RemoteException;
        
    //ChandyLamport (Snapshots)
    public boolean takeSnapshot(Map<Integer, Integer> timestamp) throws RemoteException;
    
    //Election
    /**
     * Sends out a leader nomination to all other servers.
     * Clients will return an ID so that the process can decide if 
     * won the election or not.
     */
    public int getBestLeader(int candidateID, Map<Integer, Integer> timestamp) throws RemoteException;
    public void setLeader(int leader, Map<Integer, Integer> timestamp) throws RemoteException;
    
    //File sending
    public void sendFile(File file, Map<Integer, Integer> timestamp) throws RemoteException;
}
