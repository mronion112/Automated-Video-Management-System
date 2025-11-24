package org.example;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.*;
import java.security.GeneralSecurityException;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateSheet {

    private static final String APPLICATION_NAME = "Drive-To-Sheet Sync Tool";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // ⚙️ Quyền truy cập Google API
    private static final List<String> SCOPES = Arrays.asList(
            DriveScopes.DRIVE,
            SheetsScopes.SPREADSHEETS
    );

    private static final String FFMPEG_DIR = "ffmpeg/";

    public static boolean updateSheet(
            NetHttpTransport HTTP_TRANSPORT,
            String sheetLink,
            String sheetName,
            String driveParentId,
            Credential credential,
            String TOKENS_DIRECTORY_PATH,
            String CREDENTIALS_FILE_PATH
    ) throws IOException, GeneralSecurityException {

        // Lấy Sheet ID
        String sheetId = extractSheetId(sheetLink);
        if (sheetId == null) {
            System.err.println("Link Sheet không hợp lệ!");
            return false;
        }

        // ⚙️ Khởi tạo dịch vụ
        Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        Sheets sheetService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        // ✅ Kiểm tra sheet tồn tại
        boolean sheetExists = sheetService.spreadsheets().get(sheetId)
                .setFields("sheets.properties")
                .execute()
                .getSheets()
                .stream()
                .anyMatch(s -> s.getProperties().getTitle().equalsIgnoreCase(sheetName));

        if (!sheetExists) {
            System.err.println("❌ Không tìm thấy sheet: " + sheetName);
            return false;
        }

        // Đọc dữ liệu cũ (A3:D)
        String readRange = "'" + sheetName + "'!A3:D";
        ValueRange oldDataResp = sheetService.spreadsheets().values()
                .get(sheetId, readRange)
                .execute();

        List<List<Object>> oldSheet = oldDataResp.getValues() != null
                ? oldDataResp.getValues()
                : new ArrayList<>();

        // Bỏ cột STT cũ (chỉ giữ 3 cột còn lại)
        List<List<Object>> cleanedOldSheet = new ArrayList<>();
        for (List<Object> row : oldSheet) {
            if (row.size() >= 4) {
                cleanedOldSheet.add(row.subList(1, 4));
            } else if (row.size() == 3) {
                cleanedOldSheet.add(row);
            }
        }

        // 📋 Lưu ID các folder đã có (từ link cột B)
        Set<String> existingFolderNames = new HashSet<>();
        for (List<Object> row : cleanedOldSheet) {
            if (row.size() > 1) {
                String link = row.get(1).toString().trim();
                String id = extractDriveFolderId(link);
                if (id != null) existingFolderNames.add(id);
            }
        }

        // 🔍 Quét tất cả folder (không giới hạn 100)
        String query = String.format(
                "'%s' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false",
                driveParentId
        );

        List<File> allFoldersList = new ArrayList<>();
        String pageToken = null;

        do {
            FileList result = driveService.files().list()
                    .setQ(query)
                    .setFields("nextPageToken, files(id, name)")
                    .setPageSize(1000) // Tối đa 1000 mỗi lần
                    .setPageToken(pageToken)
                    .execute();

            allFoldersList.addAll(result.getFiles()); // gộp vào danh sách chính
            pageToken = result.getNextPageToken();

        } while (pageToken != null);

        // ✅ Tạo đối tượng FileList thủ công cho code cũ
        FileList allFolders = new FileList();
        allFolders.setFiles(allFoldersList);

        if (allFolders.getFiles().isEmpty()) {
            System.out.println("Không có thư mục nào trong Drive ID: " + driveParentId);
            return false;
        }




        System.out.println("Đã tìm thấy " + allFolders.getFiles().size() + " thư mục trong Drive.");

        // 🆕 Lọc thư mục mới
        List<File> newFolders = new ArrayList<>();
        for (File f : allFolders.getFiles()) {
            if (!existingFolderNames.contains(f.getId())) newFolders.add(f);
        }

        System.out.printf("Có %d thư mục mới cần thêm.%n", newFolders.size());
        if (newFolders.isEmpty()) {
            System.out.println("✅ Không có folder mới nào, kết thúc!");
            return true;
        }


        List<List<Object>> newRows = new ArrayList<>();

        int count = 1;
        for (File folder : newFolders) {
            String folderId = folder.getId();
            String folderName = folder.getName();
            String driveLink = "https://drive.google.com/drive/folders/" + folderId;

            String videoQuery = String.format("'%s' in parents and mimeType contains 'video/' and trashed=false", folderId);
            FileList videos = driveService.files().list()
                    .setQ(videoQuery)
                    .setFields("files(id, name, videoMediaMetadata(durationMillis))")
                    .execute();

            long duration = 0;
            newRows.add(Arrays.asList(folderName, driveLink, "Chưa thực hiện"));
            System.out.println("Đã load "+count+"/"+newFolders.size());
            System.out.println(folderName);
            count++;
        }

        // 🧩 Gộp dữ liệu mới + cũ
        List<List<Object>> finalSheet = new ArrayList<>();
        finalSheet.addAll(newRows);
        finalSheet.addAll(cleanedOldSheet);

        // 🔢 Đánh lại STT cố định 4 cột
        List<List<Object>> numberedSheet = new ArrayList<>();
        for (int i = 0; i < finalSheet.size(); i++) {
            List<Object> row = new ArrayList<>();
            row.add(i + 1); // STT
            row.addAll(finalSheet.get(i));
            while (row.size() < 4) row.add(""); // đảm bảo đúng 4 cột
            numberedSheet.add(row);
        }

        // 🧽 Xóa sạch vùng cũ trước khi ghi
        sheetService.spreadsheets().values()
                .clear(sheetId, readRange, new ClearValuesRequest())
                .execute();

        // ✏️ Ghi lại toàn bộ
        ValueRange body = new ValueRange().setValues(numberedSheet);
        sheetService.spreadsheets().values()
                .update(sheetId, readRange, body)
                .setValueInputOption("RAW")
                .execute();

        System.out.printf("✅ Đã thêm %d folder mới — tổng cộng %d dòng.%n", newRows.size(), numberedSheet.size());
        return true;
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
