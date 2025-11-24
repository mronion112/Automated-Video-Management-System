package org.example;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static String extractDriveId(String url) {
        if (url == null || url.isBlank()) return null;

        // Regex bắt tất cả pattern Drive phổ biến
        String regex = "(?<=/folders/|/file/d/|id=)[a-zA-Z0-9_-]+";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(regex).matcher(url);

        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        Scanner kb =  new Scanner(System.in);
        System.out.println("""
            
            ███╗   ███╗██████╗      ██████╗ ███╗   ██╗██╗███╗   ██╗ ██████╗\s
                    ████╗ ████║██╔══██╗    ██╔═══██╗████╗  ██║██║████╗  ██║██╔════╝\s
                    ██╔████╔██║██████╔╝    ██║   ██║██╔██╗ ██║██║██╔██╗ ██║██║  ███╗
                    ██║╚██╔╝██║██╔══██╗    ██║   ██║██║╚██╗██║██║██║╚██╗██║██║   ██║
                    ██║ ╚═╝ ██║██║  ██║    ╚██████╔╝██║ ╚████║██║██║ ╚████║╚██████╔╝
                    ╚═╝     ╚═╝╚═╝  ╚═╝     ╚═════╝ ╚═╝  ╚═══╝╚═╝╚═╝  ╚═══╝ ╚═════╝\s
                                               Mr.Onion
        Welcome to the OnionTool google drive 
        + We support for only https://drive.google.com/ website
        + You MUST have the GoogleDrive account permission to use this application
        NOTICE : The tool may open the sign gg website, that will have security warning but don't worry just continue.
         
        However enjoy my tool ヾ(≧▽≦*)o
                    """);
    
        NetHttpTransport transport = new NetHttpTransport();
        String credentialFilePath = "GoogleJsonDataDzung.json";
        String tokenFilePath = "Token";

        Credential credential = CredentialModun.getCredentials(transport, credentialFilePath, tokenFilePath);

        System.out.print("OutPut folder : ");
        String localFolderPath = kb.nextLine();

        System.out.print("Link driveFolder : ");
        String driveFolderPath = kb.nextLine();

        System.out.print("Link google sheet : ");
        String linkSheet = kb.nextLine();
        System.out.print("Name sheet : ");
        String nameSheet = kb.nextLine();


        File folder = new File(localFolderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Local folder invalid: " + localFolderPath);
            return;
        }

        //Lấy danh sách đầy đủ đường dẫn tuyệt đối
        List<String> localFolderPaths = Arrays.stream(folder.listFiles())
                .map(File::getAbsolutePath)
                .toList();

        if(localFolderPaths.isEmpty()) {
            System.out.println("Nothing to up");
        }
        final NetHttpTransport HTTP_TRANSPORTDOWNLOAD = GoogleNetHttpTransport.newTrustedTransport();

        boolean uploadDrive =  UploadDrive.uploadFolder(HTTP_TRANSPORTDOWNLOAD,localFolderPaths, extractDriveId(driveFolderPath), credential, tokenFilePath, credentialFilePath);

        if(uploadDrive){
            System.out.println("Uploaded successfully!");
        }
        else{
            System.out.println("Failed to upload!");
        }


        boolean updateSheet = UpdateSheet.updateSheet(HTTP_TRANSPORTDOWNLOAD,linkSheet,nameSheet ,extractDriveId(driveFolderPath), credential, tokenFilePath, credentialFilePath);

        if(updateSheet){
            System.out.println("Sheet updated successfully!");
        }
        else{
            System.out.println("Failed to update sheet!");
        }

    }
}
