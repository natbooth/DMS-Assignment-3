/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import java.io.File;

/**
 *
 * @author Tomas
 */
public class Snapshot
{
    File[] files;

    public Snapshot(File[] files)
    {
        this.files = files;
    }
    
    public File[] getFiles()
    {
        return files;
    }

    public void setFiles(File[] files)
    {
        this.files = files;
    }
    
    @Override
    public String toString()
    {
        String fileNames = "[";
        for (File f : files)
        {
            fileNames += f + ", ";
        }
        fileNames = fileNames.substring(0, fileNames.length() - 2) + "]";
        return fileNames;
    }    
}
