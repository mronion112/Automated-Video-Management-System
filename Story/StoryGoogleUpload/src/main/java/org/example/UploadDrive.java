package org.example;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;


public class UploadDrive {

    private static final String APPLICATION_NAME = "Drive Folder Upload OAuth";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    /**
     *  Đồng bộ thư mục cục bộ lên Google Drive
     * - Quét tất cả folder trong Drive parent
     * - So sánh với thư mục local
     * - Chỉ upload folder nào CHƯA có trong Drive
     */
    public static boolean uploadFolder(
            NetHttpTransport HTTP_TRANSPORT,
            List<String> localPaths,
            String driveParentId,
            Credential credential,
            String TOKENS_DIRECTORY_PATH,
            String CREDENTIALS_FILE_PATH
    ) throws IOException, GeneralSecurityException {

        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        System.out.println("🔍 Đang quét thư mục trong Google Drive...");
        Set<String> existingDriveFolders = new HashSet<>();

        // 1️⃣ Quét toàn bộ thư mục trong Drive parent

        String query = String.format(
                "'%s' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false",
                driveParentId
        );

        String pageToken = null;
        int total = 0;

        do {
            FileList driveFolders = service.files().list()
                    .setQ(query)
                    .setFields("nextPageToken, files(id, name)")
                    .setPageSize(1000) // có thể tối đa 1000
                    .setPageToken(pageToken)
                    .execute();

            for (File f : driveFolders.getFiles()) {
                if (f.getName() != null) {
                    existingDriveFolders.add(f.getName().trim());
                    total++;
                }
            }

            pageToken = driveFolders.getNextPageToken();
        } while (pageToken != null);

        System.out.printf("✅ Đã tìm thấy tổng cộng %d thư mục trong Drive.%n", total);
        System.out.println("────────────────────────────────────────────");


        // 2️⃣ Quét thư mục cục bộ
        Set<String> localFolders = new HashSet<>();
        for (String path : localPaths) {
            java.io.File localItem = new java.io.File(path);
            if (localItem.exists() && localItem.isDirectory()) {
                localFolders.add(localItem.getName().trim());
            }
        }

        System.out.printf("Đã quét %d thư mục trong máy.%n", localFolders.size());

        // 3️⃣ Tìm folder chỉ có trong máy (chưa có trên Drive)
        Set<String> newFolders = new HashSet<>(localFolders);
        newFolders.removeAll(existingDriveFolders);

        if (newFolders.isEmpty()) {
            System.out.println("Không có thư mục mới để upload. Drive đã đồng bộ!");
            return true;
        }

        System.out.printf("Có %d thư mục mới cần upload lên Drive.%n", newFolders.size());
        System.out.println("────────────────────────────────────────────");

        // 4️⃣ Upload từng folder chưa có
        int addFolder = 1;
        int count = 1;
        for (String path : localPaths) {
            java.io.File localItem = new java.io.File(path);
            if (!localItem.isDirectory()) continue;

            if (!newFolders.contains(localItem.getName().trim())) {
                System.out.println("⏩ Bỏ qua (đã có trên Drive): " + localItem.getName());
                count++;
                continue;
            }

            System.out.printf("[%d/%d] Upload folder mới: %s%n",
                    count, newFolders.size(), localItem.getName());

            uploadFolderRecursive(service, localItem, driveParentId);
            count++;
            addFolder++;
        }

        System.out.println("Đồng bộ hoàn tất — chỉ upload các thư mục mới! " + addFolder + " folders ");
        return true;
    }

    /* ==========================================
       🔹 Upload folder con (và các file bên trong)
       ========================================== */
    private static void uploadFolderRecursive(Drive service, java.io.File localFolder, String driveParentId) throws IOException {
        File folderMetadata = new File();
        folderMetadata.setName(localFolder.getName());
        folderMetadata.setMimeType("application/vnd.google-apps.folder");
        folderMetadata.setParents(Collections.singletonList(driveParentId));

        File driveFolder = service.files().create(folderMetadata)
                .setFields("id, name")
                .execute();

        System.out.printf("Tạo thư mục Drive: %s (ID: %s)%n", driveFolder.getName(), driveFolder.getId());
        System.out.println("────────────────────────────────────────────");

        java.io.File[] files = localFolder.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("Folder trống: " + localFolder.getName());
            return;
        }

        for (java.io.File f : files) {
            if (f.isFile()) {
                uploadSingleFile(service, f, driveFolder.getId());
            } else if (f.isDirectory()) {
                uploadFolderRecursive(service, f, driveFolder.getId());
            }
        }
    }

    /* ==========================================
       🔹 Upload file đơn trong folder (với thanh tiến trình)
       ========================================== */
    private static void uploadSingleFile(Drive service, java.io.File file, String driveParentId) throws IOException {
        String mimeType = java.nio.file.Files.probeContentType(file.toPath());
        if (mimeType == null) mimeType = "application/octet-stream";

        File fileMetadata = new File();
        fileMetadata.setName(file.getName());
        fileMetadata.setParents(Collections.singletonList(driveParentId));

        FileContent mediaContent = new FileContent(mimeType, file);

        Drive.Files.Create request = service.files().create(fileMetadata, mediaContent);
        request.getMediaHttpUploader().setDirectUploadEnabled(false);
        request.getMediaHttpUploader().setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE * 20);

        // 🔸 Hiển thị tiến trình upload
        request.getMediaHttpUploader().setProgressListener(uploader -> {
            switch (uploader.getUploadState()) {
                case INITIATION_STARTED ->
                        System.out.println("Bắt đầu upload file: " + file.getName());
                case INITIATION_COMPLETE ->
                        System.out.println("Chuẩn bị upload: " + file.getName());
                case MEDIA_IN_PROGRESS -> {
                    double percent = uploader.getProgress() * 100;
                    System.out.printf("\rUploading %-30s ... %.2f%%", file.getName(), percent);
                    System.out.flush();
                }
                case MEDIA_COMPLETE ->
                        System.out.printf("\rHoàn tất upload: %-30s%n", file.getName());
            }
        });

        File uploaded = request.setFields("id, name, webViewLink").execute();
        System.out.println("Uploaded file: " + uploaded.getName());
        System.out.println("Link: " + uploaded.getWebViewLink());
        System.out.println("────────────────────────────────────────────");
    }
}
