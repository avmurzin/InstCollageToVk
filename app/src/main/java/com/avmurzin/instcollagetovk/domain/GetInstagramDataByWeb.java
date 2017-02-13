package com.avmurzin.instcollagetovk.domain;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.avmurzin.instcollagetovk.activity.PhotoPickerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Реализация интерфейса {@link GetInstagramData} доступа к Инстаграм путем обращения к
 * общедоступному web-интерфейсу.
 *
 * Created by Andrey V. Murzin (http://avmurzin.com) on 14.06.16.
 *
 * @author murzin
 * @version 0.1
 */
public class GetInstagramDataByWeb implements GetInstagramData {
    private ArrayList<InstagramPost> postList;

    public GetInstagramDataByWeb() {
        postList = new ArrayList<InstagramPost>();
    }

    public int getPostCount() {
        return 0;
    }

    public ArrayList<InstagramPost> getTopPostList(String username, int listSize) {
        String responseJSON = "";
        JSONObject dataJson;
        int likes;
        boolean moreAvailable = true;
        String max_id = "0";
        InstagramPost post;
        Bitmap bitmap;
        File smallImgFile;
        RestTemplate restTemplate;
        HttpHeaders requestHeaders;
        HttpEntity requestEntity;
        int reportCount = 0;
        int test = 0;

        //обработка JSON данных, полученных от Инстаграм
        do {
            try {

                dataJson = new JSONObject(getJSON("https://www.instagram.com/" + username + "/media/?max_id=" + max_id));
                moreAvailable = dataJson.getBoolean("more_available");
                JSONArray items = dataJson.getJSONArray("items");

                Log.e("ByWeb", "JSON = " + dataJson);

                for (int i = 0; i < items.length(); i++) {
                    reportCount++;
                    JSONObject item = items.getJSONObject(i);

                    //обрабатывать только объекты типа "изображение"
                    if (item.getString("type").equals("image")) {

                        post = new InstagramPost();
                        post.setLikes(item.getJSONObject("likes").getInt("count"));
                        post.setId(item.getString("id"));
                        max_id = item.getString("id");
                        JSONObject image = item.getJSONObject("images").getJSONObject("standard_resolution");
                        post.setUrl(image.getString("url"));
                        post.setHeight(image.getInt("height"));
                        post.setWidth(image.getInt("width"));
                        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
                        Date date = new Date(Long.parseLong(item.getString("created_time")) * 1000);
                        post.setDate(formatter.format(date));
                        post.setTitle(item.getJSONObject("user").getString("username"));
                        if (postList.size() < listSize) {
                            postList.add(post);
                        } else {
                            int j = getMoreSmall(post, postList);
                            if (j >= 0) {
                                postList.remove(j);
                                postList.add(post);
                            }
                        }
                    }
                }

            } catch (JSONException e) {
                post = null;
                moreAvailable = false;
            }

            notifyBroadcast(MainParameters.NETWORK_ACTION, "Обработано " + reportCount + " публикаций");
        } while (moreAvailable);

        notifyBroadcast(MainParameters.NETWORK_ACTION, "Загрузка лучших публикаций");

        //загрузка изображений выбранных постов
        for (int i = 0; i < postList.size(); i++) {
            post = postList.get(i);
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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
            }

            post.setLocalPath(smallImgFile.getAbsolutePath());
        }

        return postList;
    }

    /**
     * Широковещательное оповещение о состоянии процесса загрузки данных Инстаграм
     * @param action Action для intent
     * @param message Сообщение, содержащее информацию о состоянрии загрузки данных
     */
    private void notifyBroadcast(String action, String message) {
        Intent intent = new Intent();
        intent.putExtra("message", message);
        intent.setAction(action);
        PhotoPickerActivity.pickerContext.sendBroadcast(intent);
    }

    /**
     * Получение JSON-строки путем запроса указанного URL
     * @param url
     * @return Строка, содержащия (предположительно) данные в формате JSON
     */
    private String getJSON(String url) {
        //
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        HttpHeaders requestHeaders = new HttpHeaders();
        HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {

        } catch (Exception e) {

        }
        return "";
    }

    private int getMoreSmall(InstagramPost post, ArrayList<InstagramPost> postList) {
        for (int i = 0; i < postList.size(); i++) {
            if (post.getLikes() > postList.get(i).getLikes()) {
                return i;
            }
        }
        return -1;
    }

    // Вспомогательные методы для генерации имен файлов для сохранения изображений.
    private static Uri getOutputMediaFileUri(){
        return Uri.fromFile(getOutputMediaFile());
    }

    private static File getOutputMediaFile(){
       File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), MainParameters.TMP_IMAGE_FOLDER);
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + Math.round(Math.random()*100) + ".jpg");
        return mediaFile;
    }
}
