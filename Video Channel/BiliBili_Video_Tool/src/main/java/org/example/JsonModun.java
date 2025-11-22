package org.example;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class JsonModun {
    private String FilePath;
    private HashMap<String, Video> LocalData = new HashMap<>();

    public JsonModun(String filePath) {
        this.FilePath = filePath;
        LocalData = null;
    }

    public void getJsonData(){

        HashMap<String, Video> Data = new HashMap<>();
        try{
            FileReader rd = new FileReader(FilePath);
            Type type = new TypeToken<HashMap<String,Video>>(){}.getType();
            Data = new Gson().fromJson(rd,type);

        }catch(IOException e){
            System.out.println("Error reading JsonFile ..." + FilePath + "/DataScrap.json");
        }
        catch (JsonIOException e){
            System.out.println("Error getJson Data to HashMap ..." + FilePath + "/DataScrap.json");
        }

        LocalData = Data;

    }



    public String getFilePath() {
        return FilePath;
    }

    public void setFilePath(String filePath) {
        FilePath = filePath;
    }

    public HashMap<String, Video> getLocalData() {
        return LocalData;
    }

    public void setLocalData(HashMap<String, Video> localData) {
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
