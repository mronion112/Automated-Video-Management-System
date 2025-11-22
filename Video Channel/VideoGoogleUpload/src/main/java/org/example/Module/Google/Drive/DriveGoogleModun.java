package org.example.Module.Google.Drive;

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
import org.example.Module.Local.FolderModun;
import org.example.Module.Local.VideoModun;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class DriveGoogleModun {
    private static final String APPLICATION_NAME = "Drive Folder Upload OAuth";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    private NetHttpTransport HTTP_TRANSPORT;
    private List<FolderModun> localPaths;
    private String driveParentId;
    private Credential credential;
    private String TOKENS_DIRECTORY_PATH;
    private String CREDENTIALS_FILE_PATH;
    private Set<String> existingDriveFolders = new HashSet<>();

    private Drive service;
    private String query;


    public DriveGoogleModun(NetHttpTransport HTTP_TRANSPORT, List<FolderModun> localPaths, String driveParentId, Credential credential, String CREDENTIALS_FILE_PATH, String TOKENS_DIRECTORY_PATH) {
        this.HTTP_TRANSPORT = HTTP_TRANSPORT;
        this.localPaths = localPaths;
        this.driveParentId = driveParentId;
        this.credential = credential;
        this.CREDENTIALS_FILE_PATH = CREDENTIALS_FILE_PATH;
        this.TOKENS_DIRECTORY_PATH = TOKENS_DIRECTORY_PATH;
    }

    public void setUpDriveConnect(){
        service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        query = String.format(
                "'%s' in parents and mimeType='application/vnd.google-apps.folder' and trashed=false",
                driveParentId
        );
    }

    public void getExistingDriveFolders() throws IOException {
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

        System.out.printf("Đã tìm thấy tổng cộng %d thư mục trong Drive.%n", total);
        System.out.println("────────────────────────────────────────────");
    }


    public HashMap<String, DriveObjectModun> uploadDriveStarted(HashMap<String, VideoModun> localFolders, String localFolderPath) throws IOException {

        //localFolders : NameFile : Duration ...

        HashMap<String, VideoModun> newFolders = new HashMap<>();
        HashMap<String, DriveObjectModun> listFolderUpSheet = new HashMap<>();

        if(localFolders.isEmpty()){
            System.out.println("Không có gì để thêm mới listLocalData = 0");
            return new HashMap<>() ;
        }

        int skipVideo = 1;

        int countCheck = 1;
        System.out.println("Check list video on drive");

        for(String nameFolder : localFolders.keySet()){
            System.out.println(countCheck+"/"+existingDriveFolders.size());
            if(existingDriveFolders.contains(nameFolder)){
                skipVideo++;
                System.out.println("Skip...");
            }
            else{
                newFolders.put(nameFolder, localFolders.get(nameFolder));
                System.out.println("Found new folder: ");

            }
            countCheck++;
        }
        if(skipVideo == existingDriveFolders.size()){
            System.out.println("Skip "+skipVideo+"/"+existingDriveFolders.size() +" on drive");
            System.out.println("Không có thư mục mới để upload. Drive đã đồng bộ!");
            return new HashMap<>();
        }

        if (newFolders.isEmpty()) {
            System.out.println("Không có thư mục mới để upload. Drive đã đồng bộ!");
            return new HashMap<>();
        }


        System.out.printf("Có %d thư mục mới cần upload lên Drive.%n", newFolders.size());
        System.out.println(newFolders);
        System.out.println("────────────────────────────────────────────");

        // 4️⃣ Upload từng folder chưa có
        int addFolder = 1;
        int count = 1;
        for (String videoName : newFolders.keySet()) {

            Path fileDir = Path.of(localFolderPath, videoName);

            java.io.File localItem = new java.io.File(fileDir.toUri());

            if(!localItem.isDirectory() || !localItem.exists()) {
                System.err.printf("Bỏ qua: Đường dẫn không tồn tại hoặc không phải là thư mục: %s%n", localItem.getAbsolutePath());
                continue; // Bỏ qua mục này và chuyển sang mục tiếp theo
            }

            System.out.printf("[%d/%d] Upload folder mới: %s%n",
                    count, newFolders.size(), localItem.getName());

            String driveLink =  uploadFolderRecursive(service, localItem, driveParentId);

            if (driveLink == null) {
                System.out.println("Lỗi không nhận được đường link folder ");
            }

            listFolderUpSheet.put(videoName, new DriveObjectModun(videoName ,driveLink,newFolders.get(videoName).getDuration()));

            count++;
            addFolder++;
        }

        System.out.println("Đồng bộ hoàn tất — chỉ upload các thư mục mới! " + (addFolder-1) + " folders ");
        return listFolderUpSheet;

    }

    public NetHttpTransport getHTTP_TRANSPORT() {
        return HTTP_TRANSPORT;
    }

    private static String uploadFolderRecursive(Drive service, java.io.File localFolder, String driveParentId) throws IOException {
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
            return null;
        }

        for (java.io.File f : files) {
            if (f.isFile()) {
                uploadSingleFile(service, f, driveFolder.getId());
            } else if (f.isDirectory()) {
                uploadFolderRecursive(service, f, driveFolder.getId());
            }
        }
        return "https://drive.google.com/drive/folders/"+driveFolder.getId();
    }

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


    public void setHTTP_TRANSPORT(NetHttpTransport HTTP_TRANSPORT) {
        this.HTTP_TRANSPORT = HTTP_TRANSPORT;
    }

    public List<FolderModun> getLocalPaths() {
        return localPaths;
    }

    public void setLocalPaths(List<FolderModun> localPaths) {
        this.localPaths = localPaths;
    }

    public String getDriveParentId() {
        return driveParentId;
    }

    public void setDriveParentId(String driveParentId) {
        this.driveParentId = driveParentId;
    }

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public String getTOKENS_DIRECTORY_PATH() {
        return TOKENS_DIRECTORY_PATH;
    }

    public void setTOKENS_DIRECTORY_PATH(String TOKENS_DIRECTORY_PATH) {
        this.TOKENS_DIRECTORY_PATH = TOKENS_DIRECTORY_PATH;
    }

    public String getCREDENTIALS_FILE_PATH() {
        return CREDENTIALS_FILE_PATH;
    }

    public void setCREDENTIALS_FILE_PATH(String CREDENTIALS_FILE_PATH) {
        this.CREDENTIALS_FILE_PATH = CREDENTIALS_FILE_PATH;
    }


}
