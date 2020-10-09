package com.sparklit.adbutler;
import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

public class FrequencyCappingManager {
    private String freqCapFilename = "ab_freq_cap.txt";
    private Context context;
    private Set<FrequencyCappingData> data;

    public FrequencyCappingManager(Context context){
        this.context = context;
        this.setData(new HashSet<FrequencyCappingData>());
        readFile();
    }

    public void parseResponseData(Placement p){
        String placementID = p.getPlacementID();
        String views = p.getViews();
        String start = p.getStart();
        String expiry = p.getExpiry();
        updateData(placementID, views, start, expiry);
    }

    public void updateData(String placementID, String views, String start, String expiry){
        boolean found = false;
        if(views.equals("") || start.equals("") || expiry.equals("")){
            return;
        }
        for(FrequencyCappingData datum : getData()){
            if(datum.getPlacementID().equals(placementID)){
                found = true;
                datum.setViews(views);
                datum.setExpiry(expiry);
                datum.setStart(start);
                writeFile();
                break;
            }
        }
        if(!found){
            FrequencyCappingData datum = new FrequencyCappingData();
            datum.setPlacementID(placementID);
            datum.setViews(views);
            datum.setExpiry(expiry);
            datum.setStart(start);
            getData().add(datum);
            writeFile();
        }
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
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            long timestampLong = timestamp.getTime();
            this.setData(new HashSet<FrequencyCappingData>());
            StringBuilder stringBuilder = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(streamReader);
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append('\n');
                    String[] splitStr = line.split(",");
                    if(Long.parseLong(splitStr[3]) * 1000 >= timestampLong){
                        FrequencyCappingData newData = new FrequencyCappingData();
                        newData.setPlacementID(splitStr[0]);
                        newData.setViews(splitStr[1]);
                        newData.setStart(splitStr[2]);
                        newData.setExpiry(splitStr[3]);
                        getData().add(newData);
                    }
                    line = reader.readLine();
                }
            } catch (IOException e) {
                // Error occurred when opening raw file for reading.
            } finally {
                String contents = stringBuilder.toString();
            }
        }
    }

    // Will run only if responses contain frequency capping data
    public void writeFile(){
        String filename = freqCapFilename;
        StringBuilder builder = new StringBuilder();
        for(FrequencyCappingData datum : getData()){
            builder.append(datum.getPlacementID()).append(",").append(datum.getViews()).append(",").append(datum.getStart()).append(",").append(datum.getExpiry()).append("\n");
        }
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(builder.toString().getBytes());
        }catch(FileNotFoundException ex){
            try {
                File newFile = new File(freqCapFilename);
                newFile.createNewFile();
                FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
                fos.write(builder.toString().getBytes());
            } catch (IOException e) {
                System.out.println("An error occurred while creating frequency capping file.");
                e.printStackTrace();
            }
        } catch(Exception e){
            System.out.println("An error occurred while writing to frequency capping file.");
        }
    }

    public Set<FrequencyCappingData> getData() {
        return data;
    }

    public void setData(Set<FrequencyCappingData> data) {
        this.data = data;
    }
}
