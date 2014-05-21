/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Nat
 */
public class UserInterface extends javax.swing.JFrame
{

    RMIServer server;
    JList displayList;

    /**
     * Creates new form UserInterface
     *
     * @param server
     */
    public UserInterface(final RMIServer server)
    {

        // Set reffrence to server
        this.server = server;

        // Setup GUI
        this.setTitle("DMS Assignment 3");

        this.setSize(854 + 2, 480 + 26);
        this.setMinimumSize(new java.awt.Dimension(854 + 2, 480 + 26));
        this.setPreferredSize(new java.awt.Dimension(854 + 2, 480 + 26));
        //this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        //this.setIconImage(new ImageIcon(getClass().getResource("/logo.png")).getImage());
        this.setLookAndFeel();
        //this.setUndecorated(true);

        //Initilise Components
        initComponents();

        // Update Start/Stop Server Button
        jTextFieldCoordinatorAddress.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                if (jTextFieldCoordinatorAddress.getText().equals(""))
                {
                    jButtonStartStopServer.setText("Start Server");
                } else
                {
                    jButtonStartStopServer.setText("Connect");
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                if (jTextFieldCoordinatorAddress.getText().equals(""))
                {
                    jButtonStartStopServer.setText("Start Server");
                } else
                {
                    jButtonStartStopServer.setText("Connect");
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                if (jTextFieldCoordinatorAddress.getText().equals(""))
                {
                    jButtonStartStopServer.setText("Start Server");
                } else
                {
                    jButtonStartStopServer.setText("Connect");
                }
            }
        });

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                // Hide window provide visual feedback while server shutsdown
                e.getWindow().setVisible(false);
                // Stop Server
                server.stopServer();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jPanelMain = new javax.swing.JPanel();
        jLabelTitle = new javax.swing.JLabel();
        jButtonStartStopServer = new javax.swing.JButton();
        jTextFieldCoordinatorAddress = new javax.swing.JTextField();
        jLabelCoordinatorAddress = new javax.swing.JLabel();
        jLabelElectionInformationTitle = new javax.swing.JLabel();
        jLabelServerStatusTitle = new javax.swing.JLabel();
        jLabelIPAddress = new javax.swing.JLabel();
        jTextFieldIPAddress = new javax.swing.JTextField();
        jLabelProcessID = new javax.swing.JLabel();
        jTextFieldProcessID = new javax.swing.JTextField();
        jLabelCoordinatorID = new javax.swing.JLabel();
        jTextFieldCoordinatorID = new javax.swing.JTextField();
        jLabelPort = new javax.swing.JLabel();
        jTextFieldPort = new javax.swing.JTextField();
        jButtonForceElection = new javax.swing.JButton();
        jLabelServerStatus = new javax.swing.JLabel();
        jLabelElectionStatus = new javax.swing.JLabel();
        jLabelFilesTitle = new javax.swing.JLabel();
        jButtonSyncFile = new javax.swing.JButton();
        jButtonGetFileList = new javax.swing.JButton();
        jButtonRefresh = new javax.swing.JButton();
        jLabelServersConnected = new javax.swing.JLabel();
        jTextFieldServersConnected = new javax.swing.JTextField();
        jScrollPaneFiles = new javax.swing.JScrollPane();
        jLabelSystemTime = new javax.swing.JLabel();
        jTextFieldSystemTime = new javax.swing.JTextField();
        jButtonUpdateSystemTime = new javax.swing.JButton();
        jButtonTakeSnapshot = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DMS Assignment 3");
        setPreferredSize(new java.awt.Dimension(854, 480));

        jPanelMain.setBackground(new java.awt.Color(255, 255, 255));
        jPanelMain.setPreferredSize(new java.awt.Dimension(854, 480));

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabelTitle.setText("DMS Assignment 3");

        jButtonStartStopServer.setText("Start Server");
        jButtonStartStopServer.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonStartStopServerActionPerformed(evt);
            }
        });

        jTextFieldCoordinatorAddress.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jTextFieldCoordinatorAddressActionPerformed(evt);
            }
        });

        jLabelCoordinatorAddress.setText("Coordinator Address");

        jLabelElectionInformationTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelElectionInformationTitle.setText("Election Information");

        jLabelServerStatusTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelServerStatusTitle.setText("Server Status");

        jLabelIPAddress.setText("IP Address");

        jTextFieldIPAddress.setEditable(false);
        jTextFieldIPAddress.setText(server.getLocalIPAddress());

        jLabelProcessID.setText("Process ID");

        jTextFieldProcessID.setEditable(false);
        jTextFieldProcessID.setText(Integer.toString(server.getLocalProcessID()));

        jLabelCoordinatorID.setText("Coordinator ID");

        jTextFieldCoordinatorID.setEditable(false);
        jTextFieldCoordinatorID.setText(Integer.toString(server.getLeaderID()));

        jLabelPort.setText("Port");

        jTextFieldPort.setEditable(false);
        jTextFieldPort.setText(Integer.toString(server.RMI_PORT));

        jButtonForceElection.setText("Force Election");
        jButtonForceElection.setEnabled(false);
        jButtonForceElection.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonForceElectionActionPerformed(evt);
            }
        });

        jLabelServerStatus.setBackground(new java.awt.Color(255, 0, 0));
        jLabelServerStatus.setText("Stopped");
        jLabelServerStatus.setOpaque(true);

        jLabelElectionStatus.setBackground(new java.awt.Color(255, 0, 0));
        jLabelElectionStatus.setText("Not Started");
        jLabelElectionStatus.setOpaque(true);

        jLabelFilesTitle.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabelFilesTitle.setText("Files");

        jButtonSyncFile.setText("Sync File");
        jButtonSyncFile.setEnabled(false);
        jButtonSyncFile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonSyncFileActionPerformed(evt);
            }
        });

        jButtonGetFileList.setText("Get File List");
        jButtonGetFileList.setEnabled(false);
        jButtonGetFileList.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonGetFileListActionPerformed(evt);
            }
        });

        jButtonRefresh.setText("Refresh");
        jButtonRefresh.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonRefreshActionPerformed(evt);
            }
        });

        jLabelServersConnected.setText("Servers Connected");

        jTextFieldServersConnected.setEditable(false);
        jTextFieldServersConnected.setText(Integer.toString(server.getNumServersConnected()));

        jLabelSystemTime.setText("SystemTime");

        jTextFieldSystemTime.setEditable(false);
        jTextFieldSystemTime.setText(server.getTimeAsString());

        jButtonUpdateSystemTime.setText("Update");
        jButtonUpdateSystemTime.setEnabled(false);
        jButtonUpdateSystemTime.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonUpdateSystemTimeActionPerformed(evt);
            }
        });

        jButtonTakeSnapshot.setText("Take Snapshot");
        jButtonTakeSnapshot.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonTakeSnapshotActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelMainLayout = new javax.swing.GroupLayout(jPanelMain);
        jPanelMain.setLayout(jPanelMainLayout);
        jPanelMainLayout.setHorizontalGroup(
            jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabelElectionInformationTitle)
                        .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelTitle)
                            .addComponent(jLabelServerStatusTitle)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanelMainLayout.createSequentialGroup()
                                    .addComponent(jLabelCoordinatorID)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTextFieldCoordinatorID, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanelMainLayout.createSequentialGroup()
                                    .addComponent(jLabelProcessID)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTextFieldProcessID, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanelMainLayout.createSequentialGroup()
                                    .addComponent(jLabelPort)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTextFieldPort, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanelMainLayout.createSequentialGroup()
                                    .addComponent(jLabelIPAddress)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTextFieldIPAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanelMainLayout.createSequentialGroup()
                                    .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabelCoordinatorAddress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabelServerStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanelMainLayout.createSequentialGroup()
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(jTextFieldCoordinatorAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMainLayout.createSequentialGroup()
                                            .addGap(30, 30, 30)
                                            .addComponent(jButtonStartStopServer, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMainLayout.createSequentialGroup()
                            .addComponent(jLabelServersConnected)
                            .addGap(18, 18, 18)
                            .addComponent(jTextFieldServersConnected, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMainLayout.createSequentialGroup()
                        .addComponent(jLabelElectionStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonForceElection))
                    .addGroup(jPanelMainLayout.createSequentialGroup()
                        .addComponent(jLabelSystemTime)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextFieldSystemTime, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelMainLayout.createSequentialGroup()
                        .addComponent(jButtonUpdateSystemTime)
                        .addGap(105, 105, 105)
                        .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabelFilesTitle)
                                .addComponent(jButtonGetFileList)
                                .addComponent(jScrollPaneFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jButtonSyncFile, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(jPanelMainLayout.createSequentialGroup()
                        .addComponent(jButtonTakeSnapshot)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRefresh)))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        jPanelMainLayout.setVerticalGroup(
            jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelTitle)
                    .addComponent(jButtonTakeSnapshot)
                    .addComponent(jButtonRefresh))
                .addGap(15, 15, 15)
                .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelServerStatusTitle)
                    .addComponent(jLabelFilesTitle))
                .addGap(10, 10, 10)
                .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelCoordinatorAddress)
                    .addComponent(jTextFieldCoordinatorAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonGetFileList))
                .addGap(16, 16, 16)
                .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanelMainLayout.createSequentialGroup()
                        .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButtonStartStopServer)
                            .addComponent(jLabelServerStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(16, 16, 16)
                        .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelIPAddress)
                            .addComponent(jTextFieldIPAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelPort)
                            .addComponent(jTextFieldPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14)
                        .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelProcessID)
                            .addComponent(jTextFieldProcessID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelCoordinatorID)
                            .addComponent(jTextFieldCoordinatorID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextFieldServersConnected, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelServersConnected))
                        .addGap(9, 9, 9)
                        .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextFieldSystemTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelSystemTime)
                            .addComponent(jButtonUpdateSystemTime))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelElectionInformationTitle))
                    .addComponent(jScrollPaneFiles))
                .addGap(18, 18, 18)
                .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelMainLayout.createSequentialGroup()
                        .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonForceElection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonSyncFile))
                        .addGap(70, 70, 70))
                    .addGroup(jPanelMainLayout.createSequentialGroup()
                        .addComponent(jLabelElectionStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelMain, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonStartStopServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartStopServerActionPerformed
        if (this.jButtonStartStopServer.getText().equals("Start Server"))
        {
            this.jLabelServerStatus.setText("Starting...");
            this.jLabelServerStatus.setBackground(Color.yellow);

            // Start Server
            Thread t1 = new Thread(new Runnable()
            {
                public void run()
                {
                    server.startServer(server);
                    updateInfo();
                }
            });
            t1.start();

        } else if (this.jButtonStartStopServer.getText().equals("Connect"))
        {
            this.jLabelServerStatus.setText("Connecting...");
            this.jLabelServerStatus.setBackground(Color.yellow);

            // Connect to Server
            Thread t1 = new Thread(new Runnable()
            {
                public void run()
                {
                    server.connect(jTextFieldCoordinatorAddress.getText());
                    updateInfo();
                }
            });
            t1.start();

        } else if (this.jButtonStartStopServer.getText().equals("Stop Server") || this.jButtonStartStopServer.getText().equals("Disconnect"))
        {
            this.jLabelServerStatus.setText("Stopping...");
            this.jLabelServerStatus.setBackground(Color.yellow);
            jButtonUpdateSystemTime.setEnabled(false);
            jButtonForceElection.setEnabled(false);
            jButtonGetFileList.setEnabled(false);
            jButtonSyncFile.setEnabled(false);

            // Stop Server
            Thread t1 = new Thread(new Runnable()
            {
                public void run()
                {
                    server.stopServer();
                    updateInfo();
                }
            });
            t1.start();

        }
    }//GEN-LAST:event_jButtonStartStopServerActionPerformed

    private void jButtonForceElectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonForceElectionActionPerformed
        this.jLabelElectionStatus.setText("Election Started");
        this.jLabelElectionStatus.setBackground(Color.yellow);

        // Start Election Process
        server.forceElection();
        updateInfo();

    }//GEN-LAST:event_jButtonForceElectionActionPerformed

    private void jButtonGetFileListActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonGetFileListActionPerformed
    {//GEN-HEADEREND:event_jButtonGetFileListActionPerformed
        // Get File List
        File[] fileList = server.getLeaderFileList();
        if (fileList != null)
        {
            displayList = new JList(fileList);
            displayList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            displayList.setCellRenderer(new CellRenderer());
            displayList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
            displayList.setName("displayList");
            jScrollPaneFiles.setViewportView(displayList);
        }
    }//GEN-LAST:event_jButtonGetFileListActionPerformed

    private void jButtonSyncFileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonSyncFileActionPerformed
    {//GEN-HEADEREND:event_jButtonSyncFileActionPerformed

        this.jButtonSyncFile.setText("Syncing File...");
        File selectedFile = (File) displayList.getSelectedValue();
        if (selectedFile != null)
        {
            server.getFileFromLeader(selectedFile.getName());
            JOptionPane.showMessageDialog(this, "Transfer complete.", "Transfer Complete", JOptionPane.PLAIN_MESSAGE);
        } else
        {
            JOptionPane.showMessageDialog(this, "Please select a file from the list first.", "Woops!", JOptionPane.INFORMATION_MESSAGE);
        }

        this.jButtonSyncFile.setText("Sync File");
    }//GEN-LAST:event_jButtonSyncFileActionPerformed

    private void jButtonRefreshActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonRefreshActionPerformed
    {//GEN-HEADEREND:event_jButtonRefreshActionPerformed
        updateInfo();
    }//GEN-LAST:event_jButtonRefreshActionPerformed

    private void jTextFieldCoordinatorAddressActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jTextFieldCoordinatorAddressActionPerformed
    {//GEN-HEADEREND:event_jTextFieldCoordinatorAddressActionPerformed
        jButtonStartStopServerActionPerformed(evt);
    }//GEN-LAST:event_jTextFieldCoordinatorAddressActionPerformed

    private void jButtonUpdateSystemTimeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonUpdateSystemTimeActionPerformed
    {//GEN-HEADEREND:event_jButtonUpdateSystemTimeActionPerformed
        server.syncTime();
        updateInfo();
    }//GEN-LAST:event_jButtonUpdateSystemTimeActionPerformed

    private void jButtonTakeSnapshotActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonTakeSnapshotActionPerformed
    {//GEN-HEADEREND:event_jButtonTakeSnapshotActionPerformed
        JOptionPane.showMessageDialog(this, "Snapshot is " + server.startSnapshot(), "Snapshot", JOptionPane.PLAIN_MESSAGE);

    }//GEN-LAST:event_jButtonTakeSnapshotActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                RMIServer server = new RMIServer();
                new UserInterface(server).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonForceElection;
    private javax.swing.JButton jButtonGetFileList;
    private javax.swing.JButton jButtonRefresh;
    private javax.swing.JButton jButtonStartStopServer;
    private javax.swing.JButton jButtonSyncFile;
    private javax.swing.JButton jButtonTakeSnapshot;
    private javax.swing.JButton jButtonUpdateSystemTime;
    private javax.swing.JLabel jLabelCoordinatorAddress;
    private javax.swing.JLabel jLabelCoordinatorID;
    private javax.swing.JLabel jLabelElectionInformationTitle;
    private javax.swing.JLabel jLabelElectionStatus;
    private javax.swing.JLabel jLabelFilesTitle;
    private javax.swing.JLabel jLabelIPAddress;
    private javax.swing.JLabel jLabelPort;
    private javax.swing.JLabel jLabelProcessID;
    private javax.swing.JLabel jLabelServerStatus;
    private javax.swing.JLabel jLabelServerStatusTitle;
    private javax.swing.JLabel jLabelServersConnected;
    private javax.swing.JLabel jLabelSystemTime;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JScrollPane jScrollPaneFiles;
    private javax.swing.JTextField jTextFieldCoordinatorAddress;
    private javax.swing.JTextField jTextFieldCoordinatorID;
    private javax.swing.JTextField jTextFieldIPAddress;
    private javax.swing.JTextField jTextFieldPort;
    private javax.swing.JTextField jTextFieldProcessID;
    private javax.swing.JTextField jTextFieldServersConnected;
    private javax.swing.JTextField jTextFieldSystemTime;
    // End of variables declaration//GEN-END:variables

    // Conveniance method to set look and feel
    private void setLookAndFeel()
    {
        //Set Look and Feal
        try
        {
            // Try Custom Look and Feal
            UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex)
        {
            try
            {
                // Set System L&F
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e)
            {
            }
        }
    }

    private static class CellRenderer extends DefaultListCellRenderer
    {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus)
        {
            if (value instanceof File)
            {
                File file = (File) value;
                setText(file.getName());
                setIcon(FileSystemView.getFileSystemView().getSystemIcon(file));
                if (isSelected)
                {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else
                {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                setEnabled(list.isEnabled());
                setFont(list.getFont());
                setOpaque(true);
            }
            return this;
        }
    }

    private void updateInfo()
    {
        // IP Address
        jTextFieldIPAddress.setText(server.getLocalIPAddress());
        // Port
        jTextFieldPort.setText(Integer.toString(server.RMI_PORT));
        // ProcessID
        jTextFieldProcessID.setText(Integer.toString(server.getLocalProcessID()));
        // Leader ID
        jTextFieldCoordinatorID.setText(Integer.toString(server.getLeaderID()));
        // Servers Connected
        jTextFieldServersConnected.setText(Integer.toString(server.getNumServersConnected()));
        // System Time
        this.jTextFieldSystemTime.setText(server.getTimeAsString());

        //Server Status
        String serverStatus = server.getServerStatus();
        if (serverStatus.equals("starting"))
        {
            jButtonStartStopServer.setEnabled(false);
            if (jTextFieldCoordinatorAddress.getText().equals(""))
            {
                jLabelServerStatus.setText("Starting...");
            } else
            {
                jLabelServerStatus.setText("Connecting...");
            }
            this.jLabelServerStatus.setBackground(Color.yellow);
        } else if (serverStatus.equals("running"))
        {
            jButtonStartStopServer.setEnabled(true);
            jButtonUpdateSystemTime.setEnabled(true);
            jButtonForceElection.setEnabled(true);
            jButtonGetFileList.setEnabled(true);
            jButtonSyncFile.setEnabled(true);
            if (jTextFieldCoordinatorAddress.getText().equals(""))
            {
                jLabelServerStatus.setText("Running");
                jButtonStartStopServer.setText("Stop Server");
            } else
            {
                jLabelServerStatus.setText("Connected");
                jButtonStartStopServer.setText("Disconnect");
            }
            this.jLabelServerStatus.setBackground(Color.green);
        } else if (serverStatus.equals("stopping"))
        {
            jButtonStartStopServer.setEnabled(false);
            if (jTextFieldCoordinatorAddress.getText().equals(""))
            {
                jLabelServerStatus.setText("Stopping...");
            } else
            {
                jLabelServerStatus.setText("Disconnecting...");
            }
            this.jLabelServerStatus.setBackground(Color.yellow);
        } else if (serverStatus.equals("stopped"))
        {
            jButtonStartStopServer.setEnabled(true);
            if (jTextFieldCoordinatorAddress.getText().equals(""))
            {
                jLabelServerStatus.setText("Stopped");
                jButtonStartStopServer.setText("Start Server");
            } else
            {
                jLabelServerStatus.setText("Disconnected");
                jButtonStartStopServer.setText("Connect");
            }
            this.jLabelServerStatus.setBackground(Color.red);
        }

        //Election Status
        String electionStatus = server.getElectionStatus();
        if (electionStatus.equals("notstarted"))
        {
            jButtonForceElection.setEnabled(false);
            this.jLabelElectionStatus.setText("Not Started");
            this.jLabelElectionStatus.setBackground(Color.red);
        } else if (electionStatus.equals("started"))
        {
            jButtonForceElection.setEnabled(false);
            this.jLabelElectionStatus.setText("Started");
            this.jLabelElectionStatus.setBackground(Color.yellow);
        } else if (electionStatus.equals("complete"))
        {
            jButtonForceElection.setEnabled(true);
            this.jLabelElectionStatus.setText("Complete");
            this.jLabelElectionStatus.setBackground(Color.green);
        }

    }

}
