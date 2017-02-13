package com.avmurzin.instcollagetovk.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.avmurzin.instcollagetovk.R;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.model.VKWallPostResult;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

/**
 * Activity для демонстрации коллажа и публикации его на стене ВК.
 * Используется готовый файл с изборажением, созданый в {@link PhotoPickerActivity#makeCollage} и
 * размещенный в локальном хранилище. Ссылка на файл передается в intent.
 *
 * Created by Andrey V. Murzin (http://avmurzin.com) on 14.06.16.
 *
 * @author murzin
 * @version 0.1
 */
public class CollageActivity extends Activity implements View.OnClickListener {

    /**
     * ID группы ВК для публикации (0 - группа текущего авторизованного в ВК пользователя)
     */
    public static final int TARGET_GROUP = 0;

    private Button shareToVk;
    private ImageView collageImage;
    private Intent intent;
    private String url;
    private Bitmap collage;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage);

        intent = getIntent();
        url = intent.getStringExtra("url");

        shareToVk = (Button) findViewById(R.id.shareToVk);
        collageImage = (ImageView) findViewById(R.id.collageImage);
        shareToVk.setOnClickListener(this);

        ((ImageView) collageImage.findViewById(R.id.collageImage)).setImageURI(Uri.parse(url));
        collage = BitmapFactory.decodeFile(url);
    }

    /**
     * Авторизация в ВК. Стартует отдельная Activity с запросом реквизитов
     * доступа к ВК (функция VK SDK).
     * @param v
     */
    @Override
    public void onClick(View v) {
        VKSdk.login(this, "notify", "friends", "photos", "wall");
    }

    /**
     * Callback из процесса авторизации ВК.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался
                Toast.makeText(getApplicationContext(), "Выполняется публикация", Toast.LENGTH_LONG).show();
                // ID авторизовавшегося пользователя
                userId = res.userId;
                // Подготовка HTTP-запроса для загрузки картинки для публикации на стене ВК
                VKRequest request = VKApi.uploadWallPhotoRequest(new VKUploadImage(collage, VKImageParameters.jpgImage(0.9f)), Integer.parseInt(userId), 0);
                // Выполнение запроса на загрузку
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        Toast.makeText(getApplicationContext(), "Коллаж опубликован", Toast.LENGTH_LONG).show();
                        collage.recycle();
                        VKApiPhoto photoModel = ((VKPhotoArray) response.parsedModel).get(0);
                        // Публикация загруженного изображения
                        makePost(new VKAttachments(photoModel), null);
                    }

                    @Override
                    public void onError(VKError error) {

                        Toast.makeText(getApplicationContext(), "Публикация не удалась", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {

                        Toast.makeText(getApplicationContext(), "Что-то пошло не так", Toast.LENGTH_LONG).show();
                    }
                });


            }
            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации
                Toast.makeText(getApplicationContext(), "Произошла ошибка авторизации", Toast.LENGTH_LONG).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }


    }

    private void makePost(VKAttachments attachments, String message) {
        VKRequest post = VKApi.wall().post(VKParameters.from(userId, "-" + TARGET_GROUP, VKApiConst.ATTACHMENTS, attachments, VKApiConst.MESSAGE, message));
        post.setModelClass(VKWallPostResult.class);
        post.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                if (true) {
                    VKWallPostResult result = (VKWallPostResult) response.parsedModel;
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://vk.com/id" + userId)));
                    startActivity(i);
                }
            }

            @Override
            public void onError(VKError error) {
                // Обработка ошибок вызовов  VK SDK
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}
