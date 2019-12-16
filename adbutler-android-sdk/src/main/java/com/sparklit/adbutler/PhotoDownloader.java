package com.sparklit.adbutler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  Adapted from:
 *
 *  https://android--examples.blogspot.com/2017/02/android-download-image-from-url-example.html
 */

public class PhotoDownloader {

    private ProgressDialog mProgressDialog;
    private Activity context;

    public PhotoDownloader(Activity context){
        this.context = context;
    }

    public void savePhoto(String url){
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Save Photo");
        mProgressDialog.setMessage("Downloading image file...");

        AsyncTask downloadTask = new DownloadTask().execute(stringToURL(url));
    }

    private class DownloadTask extends AsyncTask<URL,Void,Bitmap> {
        // Before the tasks execution
        protected void onPreExecute(){
            mProgressDialog.show();
        }

        // Do the task in background/non UI thread
        protected Bitmap doInBackground(URL...urls){
            URL url = urls[0];
            HttpURLConnection connection = null;

            try{
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

                return bmp;

            }catch(IOException e){
                e.printStackTrace();
            }finally{
                connection.disconnect();
            }
            return null;
        }

        // When all async task done
        protected void onPostExecute(Bitmap result){
            mProgressDialog.dismiss();

            if(result!=null){
                saveImageToInternalStorage(result);
            }else {
                // Notify user that an error occurred while downloading image
            }
        }
    }

    protected URL stringToURL(String urlString){
        try{
            URL url = new URL(urlString);
            return url;
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
        return null;
    }

    protected void saveImageToInternalStorage(Bitmap bitmap){
        String name = "download_" + new SimpleDateFormat("dd-MM-yyyy'T'hh:mm:ss").format(new Date());
        MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, name, "");
    }
}
