package org.example.Module.Repositon;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import org.example.Module.Local.VideoModun;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class JsonModun {
    private String FilePath;
    private HashMap<String, VideoModun> LocalData = new HashMap<>();

    public JsonModun(String filePath) {
        this.FilePath = filePath;
        LocalData = null;
    }

    public void getJsonData(){

        HashMap<String, VideoModun> Data = new HashMap<>();
        try{
            FileReader rd = new FileReader(FilePath);
            Type type = new TypeToken<HashMap<String,VideoModun>>(){}.getType();
            Data = new Gson().fromJson(rd,type);

        }catch(IOException e){
            System.out.println("Error reading JsonFile ..." + FilePath + "/data.json");
        }
        catch (JsonIOException e){
            System.out.println("Error getJson Data to HashMap ..." + FilePath + "/data.json");
        }

        LocalData = Data;

    }



    public String getFilePath() {
        return FilePath;
    }

    public void setFilePath(String filePath) {
        FilePath = filePath;
    }

    public HashMap<String, VideoModun> getLocalData() {
        return LocalData;
    }

    public void setLocalData(HashMap<String, VideoModun> localData) {
        LocalData = localData;
    }

    @Override
    public String toString() {
        return "JsonModun{" +
                "FilePath='" + FilePath + '\'' +
                ", LocalData=" + LocalData +
                '}';
    }
}
