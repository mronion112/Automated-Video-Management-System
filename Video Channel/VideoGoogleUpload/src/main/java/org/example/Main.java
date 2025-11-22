package org.example;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.example.Controller.LocalDataController;
import org.example.Module.Google.Drive.DriveGoogleModun;
import org.example.Module.Google.Drive.DriveObjectModun;
import org.example.Module.Google.Sheet.SheetGoogleModun;
import org.example.Module.Google.Sheet.SheetObjectModun;
import org.example.Module.Local.FolderModun;
import org.example.Module.Local.VideoModun;
import org.example.Module.Permission.CredentialModun;
import org.example.Module.Repositon.JsonModun;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


//Video Long : True
//Video Short : false ( <= 3p)
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
        Scanner scanner = new Scanner(System.in);
//        System.out.print("The localFolder : ");
//        Path rootFolderDir = Path.of(scanner.nextLine());

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

        System.out.print("OutPut folder (enter to use default) : ");
        String localFolderPath = kb.nextLine();
        localFolderPath = localFolderPath.trim();


        System.out.print("Link driveFolder : ");
        String driveFolderPath = kb.nextLine();
        driveFolderPath = driveFolderPath.trim();

        System.out.print("Link google sheet : ");
        String linkSheet = kb.nextLine();
        linkSheet = linkSheet.trim();
        System.out.print("Name sheet : ");
        String nameSheet = kb.nextLine();
        nameSheet = nameSheet.trim();


        File folderChecked = new File(localFolderPath);
        if (!folderChecked.exists() || !folderChecked.isDirectory()) {
            System.out.println("Local folder invalid: " + localFolderPath);
            return;
        }

        LocalDataController localDataModun = new LocalDataController(localFolderPath);

        localDataModun.createListLocalFolder();
        List<FolderModun> listLocalFolder = localDataModun.getListLocalFolder(); //Get List Folder Video in folder
        System.out.println("Total folder inside rootFolder is : "+listLocalFolder.size());


        File dataJsonDir = new File(localFolderPath + File.separator + "DataScrap.json");
        if(!dataJsonDir.exists()){
            System.out.println("No data found");
            return;
        }
        else{
            System.out.println("Data found");
        }

        JsonModun jsonData = new JsonModun(dataJsonDir.getAbsolutePath());
        jsonData.getJsonData();

        NetHttpTransport HTTP_TRANSPORTDOWNLOAD = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential1 = CredentialModun.getCredentials(transport, credentialFilePath, tokenFilePath);
        DriveGoogleModun driveGoogleModun = new DriveGoogleModun(HTTP_TRANSPORTDOWNLOAD, listLocalFolder, extractDriveId(driveFolderPath), credential1, credentialFilePath, tokenFilePath);

        driveGoogleModun.setUpDriveConnect();

        driveGoogleModun.getExistingDriveFolders();
        if(jsonData.getLocalData() == null){
            System.out.println("No data found in DataScrap.json");
        }

        HashMap<String, VideoModun> localDataJson = jsonData.getLocalData();

        System.out.println("There have " + localDataJson + " local data found" );
        System.out.println(localDataJson);

        //Get list new folders update form drive to upload to sheet
        HashMap<String, DriveObjectModun> listFolderUpSheet =  driveGoogleModun.uploadDriveStarted(localDataJson, localFolderPath);

        List<SheetObjectModun> listNewSheets = new ArrayList<>();

        for(DriveObjectModun driveObjectModun : listFolderUpSheet.values()){

            String status = "Chưa thực hiện";
            listNewSheets.add(new SheetObjectModun(driveObjectModun.getFolderName(), driveObjectModun.getFolderPathDirve(), driveObjectModun.getDuration(), status));
        }

        System.out.println("There have " + listNewSheets.size() + " folders will be uploaded to sheet" );

        SheetGoogleModun sheetGoogleModun = new SheetGoogleModun(HTTP_TRANSPORTDOWNLOAD, linkSheet, nameSheet, extractDriveId(driveFolderPath), credential1, credentialFilePath, tokenFilePath);
        sheetGoogleModun.setUpSheetConnect();
        //Get list OldSheet form sheet
        List<SheetObjectModun> listOldSheets =  sheetGoogleModun.getOldSheets();
        listNewSheets.addAll(listOldSheets);

        List<List<Object>> Sheets = new ArrayList<>();

        int position = 1;
        for(SheetObjectModun sheetObjectModun : listNewSheets){

            List<Object> row = new ArrayList<>();
            row.add(position);
            row.add(sheetObjectModun.getName());
            row.add(sheetObjectModun.getLinkDriveFolder());
            row.add(sheetObjectModun.getDuration());
            row.add(sheetObjectModun.getStatus());

            Sheets.add(row);
            position++;
        }

        sheetGoogleModun.deleteOldSheets();

        sheetGoogleModun.writeNewSheets(listFolderUpSheet.size(), Sheets);
        System.out.println("New Sheet is : ");
        // In tiêu đề
        String line = "+-----+--------------------------------------------------+----------------------------------------------------------------------------------+----------+---------------+";
        System.out.println(line);
        System.out.printf("|%-5s|%-50s|%-82s|%-10s|%-15s|%n",
                "STT", "TIÊU ĐỀ", "FOLDER", "LOẠI", "TRẠNG THÁI");
        System.out.println(line);

        int count = 1;
        for (SheetObjectModun s : listNewSheets) {
            System.out.printf(
                    "|%-5d|%-50s|%-82s|%-10s|%-15s|%n",
                    count,
                    truncateDisplay(s.getName(), 50),
                    truncateDisplay(s.getLinkDriveFolder(), 82),
                    truncateDisplay(s.getDuration(), 10),
                    truncateDisplay(s.getStatus(), 15)
            );
            count++;
        }

        System.out.println(line);


    }

    private static int displayWidth(char c) {
        // Chinese/Japanese/Korean and fullwidth characters = width 2
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        if (block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION ||
                block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS ||
                block == Character.UnicodeBlock.HANGUL_SYLLABLES ||
                block == Character.UnicodeBlock.HIRAGANA ||
                block == Character.UnicodeBlock.KATAKANA) {
            return 2;
        }
        return 1;
    }


    private static String truncateDisplay(String text, int maxWidth) {
        if (text == null) return "";

        StringBuilder sb = new StringBuilder();
        int width = 0;

        for (char c : text.toCharArray()) {
            int w = displayWidth(c);
            if (width + w > maxWidth - 3) {   // để dành chỗ cho "..."
                return sb.append("...").toString();
            }
            sb.append(c);
            width += w;
        }
        return sb.toString();
    }


}
