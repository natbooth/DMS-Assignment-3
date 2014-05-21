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
    public int getProcessID(Map<Integer, Integer> timestamp) throws RemoteException;

    public int getNextProcessID(Map<Integer, Integer> timestamp) throws RemoteException;

    public Map<Integer, RMIServerInterface> getServers(Map<Integer, Integer> timestamp) throws RemoteException;

    public void addServer(int processID, RMIServerInterface server, Map<Integer, Integer> timestamp) throws RemoteException;

    public void removeServer(int processID, Map<Integer, Integer> timestamp) throws RemoteException;

    // Election Methods
    public void startElection(Map<Integer, Integer> timestamp) throws RemoteException;

    public void setLeader(int leader, Map<Integer, Integer> timestamp) throws RemoteException;

    // Cristian Algorithm
    public long getTime(Map<Integer, Integer> timestamp) throws RemoteException;

    //Timestamps
    public Map<Integer, Integer> getVTimestamp(Map<Integer, Integer> timestamp) throws RemoteException;

    public void setTimestamp(Map<Integer, Integer> vectorTimestamp) throws RemoteException;

    // ChandyLamport (Snapshots)
    public boolean takeSnapshot(Map<Integer, Integer> timestamp) throws RemoteException;

    // File Transfer
    public byte[] downloadFile(String fileName, Map<Integer, Integer> timestamp) throws RemoteException;

    public File[] getFileList(Map<Integer, Integer> timestamp) throws RemoteException;

}
