package org.example.TieuHoaDanController;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FolderManagement {
    private String rootFolderDir;
    private String titleStory;
    private String dataDescription;
    private String dataStory;

    public FolderManagement(String rootFolderDir, String titleStory, String dataDescription, String dataStory) {
        this.titleStory = titleStory;
        this.rootFolderDir = rootFolderDir;
        this.dataDescription = dataDescription;
        this.dataStory = dataStory;


    }

    public boolean finnalMethod(){
        if(!createFolder()){
            return false;
        }

        createDataFile();
        createDescriptionFile();
        return true;
    }

    public boolean createFolder() {
        File RootFolder = new File(rootFolderDir);
        File file = new File(rootFolderDir, titleStory.toUpperCase());

        if (!file.exists()) {
            try {
                file.mkdir();
                System.out.println("Đã tạo folder " + file.getName());
            }catch (InvalidPathException e) {
                System.out.println("Remove all [/\\\\\\\\~#* in folder name");
                File newfile = new File(rootFolderDir, titleStory.replaceAll("[/\\\\~#*]", "").toUpperCase());
                newfile.mkdir();
                System.out.println("Đã tạo folder thay thế " + newfile.getName());
            }
        } else {
            System.out.println("Folder đã tồn tại ");
            return false;
        }

        return true;
    }

    public boolean createDataFile(){
        Path parentDir = Paths.get(rootFolderDir, titleStory.toUpperCase());
        Path fileData = parentDir.resolve("Data.txt");
        try {
            if (!Files.exists(parentDir)) {
                System.out.println("Không tồn tại thư mục: " + parentDir);
            }

            if (Files.exists(fileData)) {
                System.out.println("File đã tồn tại: " + fileData.getFileName());
                return false;
            }

            Files.createFile(fileData);
            System.out.println("Đã tạo file Data.txt: ");

        } catch (IOException e) {
            System.out.println("Lỗi tạo file: " + e.getMessage());
            return false;
        }


        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileData.toFile()))){
            writer.write(dataStory);
            System.out.println("Đã cập nhập Data.txt");
        }catch (IOException e){
            System.out.println("Lỗi cập nhập Data.txt ");
            return false;
        }
        return true;
    }

    public boolean createDescriptionFile() {
        Path parentDir = Paths.get(rootFolderDir, titleStory.toUpperCase());
        Path fileDesciption = parentDir.resolve("Desciption.txt");
        try {
            if (!Files.exists(parentDir)) {
                System.out.println("Không tồn tại thư mục: " + parentDir);
            }

            if (Files.exists(fileDesciption)) {
                System.out.println("File đã tồn tại: " + fileDesciption.getFileName());
                return false;
            }

            Files.createFile(fileDesciption);
            System.out.println("Đã tạo file Data.txt: ");

        } catch (IOException e) {
            System.out.println("Lỗi tạo file: " + e.getMessage());
            return false;
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileDesciption.toFile()))){
            writer.write(dataDescription);
            System.out.println("Đã cập nhập Description.txt");
        }catch (IOException e){
            System.out.println("Lỗi cập nhập Desciption.txt ");
            return false;
        }
        return true;
    }


}
