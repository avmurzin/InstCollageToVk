package com.avmurzin.instcollagetovk.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.avmurzin.instcollagetovk.domain.InstagramPost;
import com.avmurzin.instcollagetovk.domain.MainParameters;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class InstagramNetworkService extends IntentService {

    boolean isOnline;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Intent intent = new Intent();
        intent.setAction(MainParameters.NETWORK_ACTION);
        sendBroadcast(intent);
    }

    public InstagramNetworkService() {
        super("InstagramNetworkService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        isOnline = MainParameters.getIsOnline();

        //Check network connection
        if (!isOnline) {
            Intent intent2 = new Intent();
            intent2.setAction(MainParameters.NETWORK_ACTION);
            intent2.putExtra("message", "There is no network connection");
            sendBroadcast(intent2);
            return;
        }

        if (intent != null) {
            final String command = intent.getExtras().getString("command");
            final String url = intent.getExtras().getString("url");

            if(command.equals("testJSON")) {
                testJSON(url);
            }

        }
    }

    private void testJSON(String url) {
        Intent intent = new Intent();
        String responseJSON;
        JSONObject dataJson;
        String errorMessage = "";
        String infoMessage = "";
        int likes;
        boolean moreAvailable = false;
        InstagramPost post = new InstagramPost();
        Bitmap bitmap;
        File smallImgFile;

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        HttpHeaders requestHeaders = new HttpHeaders();
        HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        responseJSON = response.getBody();

        //обработка JSON данных, полученных от Инстаграм
        try {
            dataJson = new JSONObject(responseJSON);
            moreAvailable = dataJson.getBoolean("more_available");
            JSONArray items = dataJson.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {

                JSONObject item = items.getJSONObject(i);
                //обрабатывать только объекты типа "изображение"
                if (item.getString("type").equals("image")) {
                    post.setLikes(item.getJSONObject("likes").getInt("count"));
                    post.setId(item.getString("id"));
                    JSONObject image = item.getJSONObject("images").getJSONObject("standard_resolution");
                    post.setUrl(image.getString("url"));
                    //post.setId(image.getString("id"));


                    //download image
                    bitmap = null;
                    try {
                        final String imageUrl = post.getUrl();
                        RestTemplate imageRestTemplate = new RestTemplate();
                        requestHeaders = new HttpHeaders();
                        requestEntity = new HttpEntity(null, requestHeaders);
                        ResponseEntity<Resource> responseEntity = imageRestTemplate.exchange(imageUrl, HttpMethod.GET, requestEntity, Resource.class);
                        bitmap = BitmapFactory.decodeStream(responseEntity.getBody().getInputStream());

                    } catch (Exception e) {
                        Log.e("NetworkService", e.getMessage(), e);
                    }

                    smallImgFile = getOutputMediaFile();
                    try {
                        FileOutputStream out = new FileOutputStream(smallImgFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();
                    } catch (Exception e) {}

                    post.setLocalPath(smallImgFile.getAbsolutePath());

                    infoMessage += post.toString();

                }

            }

        } catch (JSONException e) {
            errorMessage = "Instagram error";
            post = null;
        }

        //Notify command sender about job complete
        intent.putExtra("message", infoMessage);
        intent.putExtra("errorMessage", errorMessage);
        intent.setAction(MainParameters.NETWORK_ACTION);
        post = null;
        sendBroadcast(intent);
    }


    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(){
        return Uri.fromFile(getOutputMediaFile());
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), MainParameters.TMP_IMAGE_FOLDER);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        // Create a media file name

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + Math.round(Math.random()*100) + ".jpg");
        return mediaFile;
    }
}
