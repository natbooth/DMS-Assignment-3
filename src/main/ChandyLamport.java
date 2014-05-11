package main;

///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package dms.assignment.pkg3;
//
//
//import java.util.List;
//import java.util.Map;
//
///**
// *
// * @author Tom Morton
// */
//public class ChandyLamport {
//
//    private boolean snapshotTaken;
//    private List<Process> neighbours;
//    private Map<Process, Snapshot> neighbourSnapshots;
//    private Process snapshotInitiator;
//
////Chandy-Lamport
//    public void receiveMessage(Message m) {
//        if (m instanceof Marker) {
//            if (!snapshotTaken) {
//                snapshotInitiator = m.getSender();
//                takeSnapshot();
//                for (Process client : neighbours) {
//                    MarkerMessage marker = new MarkerMessage(); //could be static ?
//                    sendMessage(client, marker);
//                }
//            }
//            neighbourSnapshots.put(m.getSender(), m.getSnapshot());
//            if (checkSnapshotCompleted()) {
//                sendSnapshots();
//            }
//        } else {
//            //something about snapshots, not got time to work out.
//        }
//    }
//
//    private void takeSnapshot() {
//        snapshotTaken = true;
//    //or put into the neighbours map
//        //data in here is any values for the system we need to keep for debugging
//    }
//
//    private void sendMessage(Process process, Message message) {
//    //generic method, am assuming that method is an abstract superclass.
//    }
//
//    private boolean checkSnapshotCompleted() {
//        if (!snapshotTaken) {
//            return false; //or check neighbours map
//        }
//        for (Process neighbour : neighbours) {
//            if (neighbourSnapshots.get(neighbour) == null) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private void sendSnapshots() {
//    //create a message by iterating through either neighbours or through a separate array dedicated to snapshots and then send to initiator.
//    }
//
//}
