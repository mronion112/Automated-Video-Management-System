package org.example.Module.Local;

public abstract class FileModun{

    private String FilePath;
    private String FileName;


    public FileModun(String filePath , String fileName) {
        FilePath = filePath;
        FileName = fileName;
    }

    public String getFilePath() {
        return FilePath;
    }

    public void setFilePath(String filePath) {
        FilePath = filePath;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }



    @Override
    public String toString() {
        return "FileModun{" +
                "FileName='" + FileName + '\'' +
                '}';
    }
}
