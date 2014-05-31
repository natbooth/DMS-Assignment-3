package main;

import java.io.File;
import java.io.Serializable;

/**
 *
 * @author Tomas
 */
public class Snapshot implements Serializable
{
    File[] files;

    /**
     * Creates a new snapshot from an array of files.
     */
    public Snapshot(File[] files)
    {
        this.files = files;
    }
    
    /**
     * Gets the files contained in this snapshot.
     */
    public File[] getFiles()
    {
        return files;
    }
    
    /**
     * Converts the list of files into a formatted 
     * list.
     */
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
