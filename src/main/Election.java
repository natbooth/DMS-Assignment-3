/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

/**
 *
 * @author Nat
 */
public class Election {
    private boolean inProgress;

    public Election(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void startElection() {
        this.inProgress = true;
    }
    
    public void endElection(){
         this.inProgress = false;
    }
    
    
    
    
    
    
}
