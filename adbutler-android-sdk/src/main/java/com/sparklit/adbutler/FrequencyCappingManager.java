package com.sparklit.adbutler;
import android.content.Context;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class FrequencyCappingManager {
    private String freqCapFilename = "ab_freq_cap.txt";
    private Context context;
    private FrequencyCappingData data;

    public FrequencyCappingManager(Context context){
        this.context = context;
        this.data = new FrequencyCappingData();
        readFile();
    }

    public void parseResponseData(String str){
        JsonParser parser = new JsonParser();
        JsonObject element =parser.parse(str).getAsJsonObject();
        int placementID = element.get("placement_id").getAsInt();
        int views = element.get("user_frequency_views").getAsInt();
        String start = element.get("user_frequency_start").getAsString();
        String expiry = element.get("user_frequency_expiry").getAsString();
    }

    public void readFile(){
        FileInputStream inputStream;
        InputStreamReader streamReader = null;
        try{
            inputStream = context.openFileInput(freqCapFilename);
            streamReader = new InputStreamReader(inputStream, Charset.defaultCharset());
        }catch(FileNotFoundException e){
            try {
                FileOutputStream fos = context.openFileOutput(freqCapFilename, Context.MODE_PRIVATE);
                fos.write("".getBytes());
                inputStream = context.openFileInput(freqCapFilename);
                streamReader = new InputStreamReader(inputStream, Charset.defaultCharset());
            }catch(Exception ex){
                return;
            }
        }

        if(streamReader != null){
            StringBuilder stringBuilder = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(streamReader);
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append('\n');
                    String[] splitStr = line.split(",");

                    data.placementMap.put();
                    line = reader.readLine();
                }
            } catch (IOException e) {
                // Error occurred when opening raw file for reading.
            } finally {
                String contents = stringBuilder.toString();
            }
        }
    }

    public void writeFile(){
        String filename = freqCapFilename;
        String fileContents = "this is a test\nThis is line 2";
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(fileContents.getBytes());
        }catch(FileNotFoundException ex){
            try {
                File newFile = new File(freqCapFilename);
                newFile.createNewFile();
                FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
                fos.write(fileContents.getBytes());
            } catch (IOException e) {
                System.out.println("An error occurred while creating frequency capping file.");
                e.printStackTrace();
            }
        } catch(Exception e){
            System.out.println("An error occurred while writing to frequency capping file.");
        }
    }
}
