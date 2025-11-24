package org.example.Controller;


import org.example.Module.Local.FolderModun;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalDataController {
    private List<FolderModun> listLocalFolder;
    private String rootFolderDir;

    public LocalDataController(String rootFolderDir) {
        this.rootFolderDir = rootFolderDir;
    }


    public void createListLocalFolder() {
        File rootFolder =  new File(rootFolderDir);

        if(!rootFolder.exists()){
            System.out.println("The folder is empty");
        }


        List<FolderModun> listFolder = new ArrayList<>();

        for(File file : rootFolder.listFiles()){
            if(file.isDirectory()) {
                listFolder.add(new FolderModun(file.getAbsolutePath()));
            }

        }

        listLocalFolder = listFolder;
    }

    public List<FolderModun> getListLocalFolder() {
        return listLocalFolder;
    }

    public void setListLocalFolder(List<FolderModun> listLocalFolder) {

        this.listLocalFolder = listLocalFolder;

    }

    public String getRootFolderDir() {
        return rootFolderDir;
    }

    public void setRootFolderDir(String rootFolderDir) {
        this.rootFolderDir = rootFolderDir;
    }

    @Override
    public String toString() {
        return "LocalDataController{" +
                ", rootFolderDir='" + rootFolderDir + '\'' +
                "listLocalFolder=" + listLocalFolder +
                '}';
    }
}

