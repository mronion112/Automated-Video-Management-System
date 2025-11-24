package org.example.Module.Google.Sheet;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SheetGoogleModun {
    private static final String APPLICATION_NAME = "Drive-To-Sheet Sync Tool";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private NetHttpTransport HTTP_TRANSPORT;
    private String sheetLink;
    private String sheetName;
    private String driveParentId;
    private Credential credential;
    private String TOKENS_DIRECTORY_PATH;
    private String CREDENTIALS_FILE_PATH;

    private Drive driveService;
    private Sheets sheetService;
    private String query;
    private String sheetId;
    private String readRange;
//    private Set<String> existingFolderNames;

    public SheetGoogleModun(NetHttpTransport HTTP_TRANSPORT,String sheetLink, String sheetName, String driveParentId, Credential credential, String CREDENTIALS_FILE_PATH, String TOKENS_DIRECTORY_PATH) {
        this.HTTP_TRANSPORT = HTTP_TRANSPORT;
        this.sheetLink = sheetLink;
        this.sheetName = sheetName;
        this.driveParentId = driveParentId;
        this.credential = credential;
        this.CREDENTIALS_FILE_PATH = CREDENTIALS_FILE_PATH;
        this.TOKENS_DIRECTORY_PATH = TOKENS_DIRECTORY_PATH;
    }

    public void setUpSheetConnect() throws IOException {
        driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        sheetService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        query = String.format(
                "'%s' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false",
                driveParentId
        );
        // Đọc dữ liệu cũ (A3:E)
        readRange = "'" + sheetName + "'!A3:E";

        sheetId = extractSheetId(sheetLink);
        boolean sheetExists = sheetService.spreadsheets().get(sheetId)
                .setFields("sheets.properties")
                .execute()
                .getSheets()
                .stream()
                .anyMatch(s -> s.getProperties().getTitle().equalsIgnoreCase(sheetName));

        if (!sheetExists) {
            System.err.println("Không tìm thấy sheet: " + sheetName);
            return;
        }

    }


    public List<SheetObjectModun> getOldSheets() throws IOException {

        ValueRange oldDataResp = sheetService.spreadsheets().values()
                .get(sheetId, readRange)
                .execute();

        List<List<Object>> oldSheet = oldDataResp.getValues() != null
                ? oldDataResp.getValues()
                : new ArrayList<>();

        if(oldSheet.isEmpty()|| oldSheet.size()==0){

            System.out.println("Up date first time, OldSheets = 0");
            return new ArrayList<>();

        }
        else {
            // Bỏ cột STT cũ (chỉ giữ 4 cột còn lại)
            List<SheetObjectModun> cleanedOldSheet = new ArrayList<>();
            int count = 1;
            for (List<Object> row : oldSheet) {
                if (row.size() >= 5) {
                    System.out.println("Check row " + count + "/" + oldSheet.size());
//                cleanedOldSheet.add(row.subList(1, 5));
                    cleanedOldSheet.add(new SheetObjectModun(row.get(1).toString().trim(), row.get(2).toString().trim(), row.get(3).toString().trim(), row.get(4).toString().trim()));
                    count++;

                } else {
                    System.out.println("The row " + count + "/" + oldSheet.size() + " is error");
                }

            }
            System.out.println("Scan done "+(count-1)+"/"+oldSheet.size());

//        for (List<Object> row : cleanedOldSheet) {
//            if (row.size() > 1) {
//                String name = row.get(0).toString().trim();
//                if (name != null) existingFolderNames.add(name);
//            }
//        }

            return cleanedOldSheet;
        }
    }

    public void deleteOldSheets() throws IOException {
        sheetService.spreadsheets().values()
                .clear(sheetId, readRange, new ClearValuesRequest())
                .execute();

    }

    public void writeNewSheets(int numberNewSheets, List<List<Object>> listNewSheets) throws IOException {
        ValueRange body = new ValueRange().setValues(listNewSheets);
        sheetService.spreadsheets().values()
                .update(sheetId, readRange, body)
                .setValueInputOption("RAW")
                .execute();

        System.out.printf("Đã thêm %d folder mới — tổng cộng %d dòng.%n", numberNewSheets, listNewSheets.size());
    }




    /** 🧩 Lấy folder ID từ link Google Drive **/
    private static String extractDriveFolderId(String link) {
        if (link == null) return null;
        Matcher m = Pattern.compile("(?<=/folders/)[a-zA-Z0-9_-]+").matcher(link);
        return m.find() ? m.group() : null;
    }



    // 🔹 Tách ID Sheet
    private static String extractSheetId(String link) {
        if (link == null) return null;
        var matcher = Pattern.compile("spreadsheets/d/([a-zA-Z0-9-_]+)").matcher(link);
        return matcher.find() ? matcher.group(1) : null;
    }



}
