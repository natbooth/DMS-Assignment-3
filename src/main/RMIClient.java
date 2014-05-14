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
public interface RMIClient extends Remote {
    
    //General methods
    public int getProcessID() throws RemoteException;
    public RMIClient getLeader() throws RemoteException;
    public Map<Integer, RMIClient> getClients() throws RemoteException;
    public void updateClients(Map<Integer, RMIClient> clients) throws RemoteException;
    public List<String> getFileList() throws RemoteException;
    public void updateFileList(List<String> fileList) throws RemoteException;
    
    //Cristian Algorithm (Timestamps)
    public Map<Integer, Integer> getVTimestamp() throws RemoteException;
    public void setTimestamp(Map<Integer, Integer> vectorTimestamp) throws RemoteException;
        
    //ChandyLamport (Snapshots)
    public boolean takeSnapshot() throws RemoteException;
    
    //Election
    /**
     * Sends out a leader nomination to all other clients.
     * Clients will return an ID so that the process can decide if 
     * won the election or not.
     * 
     * @param candidateID
     * @return
     * @throws RemoteException 
     */
    public int getBestLeader(int candidateID) throws RemoteException;
    public void setLeader(int leader) throws RemoteException;
}
