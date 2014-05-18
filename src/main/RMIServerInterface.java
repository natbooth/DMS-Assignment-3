/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

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
    public int getProcessID() throws RemoteException;
    public RMIServerInterface getLeader() throws RemoteException;
    public Map<Integer, RMIServerInterface> getServers() throws RemoteException;
    public void addServer(int processID, RMIServerInterface server) throws RemoteException;
    public void updateServers(Map<Integer, RMIServerInterface> servers) throws RemoteException;
    public List<String> getFileList() throws RemoteException;
    public void updateFileList(List<String> fileList) throws RemoteException;
    public void removeServer(int processID) throws RemoteException;
    //Cristian Algorithm (Timestamps)
    public Map<Integer, Integer> getVTimestamp() throws RemoteException;
    public void setTimestamp(Map<Integer, Integer> vectorTimestamp) throws RemoteException;
        
    //ChandyLamport (Snapshots)
    public boolean takeSnapshot() throws RemoteException;
    
    //Election
    /**
     * Sends out a leader nomination to all other servers.
     * Clients will return an ID so that the process can decide if 
     * won the election or not.
     * 
     * @param candidateID
     * @return
     * @throws RemoteException 
     */
    public int getBestLeader(int candidateID) throws RemoteException;
    public void setLeader(int leader) throws RemoteException;
    
    //File sending
}
