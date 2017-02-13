package com.avmurzin.instcollagetovk.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avmurzin.instcollagetovk.R;
import com.avmurzin.instcollagetovk.domain.GetInstagramData;
import com.avmurzin.instcollagetovk.domain.GetInstagramDataByApi;
import com.avmurzin.instcollagetovk.domain.GetInstagramDataByWeb;
import com.avmurzin.instcollagetovk.domain.InstagramPost;
import com.avmurzin.instcollagetovk.domain.MainParameters;
import com.avmurzin.instcollagetovk.domain.MakeCollage;
import com.avmurzin.instcollagetovk.domain.MakeCollageByDecrisingRectangle;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Text;

import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Activity для загрузки, демонстрации фотографий с бОльшим числом лайков и выбора части из них для
 * создания коллажа. Способ доступа к Instagram скрывается за интерфейсом {@link GetInstagramData}
 * (шаблон Strategy).
 * Реализации интерфейса для web: {@link GetInstagramDataByWeb} и для API: {@link GetInstagramDataByApi}
 *
 * Created by Andrey V. Murzin (http://avmurzin.com) on 14.06.16.
 *
 * @author murzin
 * @version 0.1
 */
public class PhotoPickerActivity extends Activity implements View.OnClickListener {

    private String username;
    private ArrayList<InstagramPost> postList = new ArrayList<InstagramPost>();
    private ListView listView;
    private ArrayAdapter<InstagramPost> adapter;
    private Button makeCollage;
    private ImageView backButton;
    private TextView selectCount;
    private TextView reportCount;
    private int maxPosts = 20;
    private int currentSelected = 0;
    private BroadcastReceiver receiver;
    public static Context pickerContext;
    private ProgressBar bar;

    /**
     * Адаптер (ListAdapter) для отображения списка загруженных изображений.
     */
    private class PostAdapter extends ArrayAdapter<InstagramPost> {
        public PostAdapter (Context context, ArrayList<InstagramPost> postList) {
            super(context, R.layout.photo_list_row, postList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            InstagramPost post = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.photo_list_row, null);
            }

            if (post.isSelected()) {
                ((ImageView) convertView.findViewById(R.id.selectPhoto)).setImageDrawable(getResources().getDrawable(R.drawable.check_1_icon));
            } else {
                ((ImageView) convertView.findViewById(R.id.selectPhoto)).setImageDrawable(getResources().getDrawable(R.drawable.check_0_icon));
            }

            ((ImageView) convertView.findViewById(R.id.rowPhoto)).setImageURI(Uri.parse(post.getLocalPath()));
            ((TextView) convertView.findViewById(R.id.photoDate)).setText(post.getDate());
            ((TextView) convertView.findViewById(R.id.photoTitle)).setText(post.getTitle());
            ((TextView) convertView.findViewById(R.id.photoLikeCount)).setText(Integer.toString(post.getLikes()));
            return convertView;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_picker);

        makeCollage = (Button) findViewById(R.id.makeCollage);
        backButton = (ImageView) findViewById(R.id.backButton);
        selectCount = (TextView) findViewById(R.id.selectCount);
        reportCount = (TextView) findViewById(R.id.reportCount);
        bar = (ProgressBar) this.findViewById(R.id.progressBar);
        makeCollage.setOnClickListener(this);
        backButton.setOnClickListener(this);

        pickerContext = getBaseContext();

        makeCollage.setVisibility(View.GONE);
        reportCount.setVisibility(View.VISIBLE);

        username = MainParameters.getUsername();
        new getPhotoTask().execute(username);

        selectCount.setText(currentSelected + " из " + maxPosts);

        listView = (ListView) findViewById(R.id.listView);

        adapter = new PostAdapter(getApplicationContext(), postList);
        listView.setAdapter(adapter);

        // Обработчик нажаний на элементы списка изображений
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                boolean selected = postList.get(position).isSelected();
                if (selected) {
                    currentSelected--;
                } else {
                    currentSelected++;
                }
                selectCount.setText(currentSelected + " из " + maxPosts);
                postList.get(position).setIsSelected(!selected);
                adapter.notifyDataSetChanged();
            }

        });

        // Обработчик бродкастов для обновления отображения процесса загрузки и обработки
        // данных Instagram
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(MainParameters.NETWORK_ACTION)) {
                    if (intent.getStringExtra("message") != null) {
                        reportCount.setText(intent.getStringExtra("message"));
                    }
                }
            }
        };

        registerReceiver(receiver, new IntentFilter(MainParameters.NETWORK_ACTION));
    }

    /**
     * Фоновый процесс загрузки и обработки данных Instagram
     */
    private class getPhotoTask extends AsyncTask<String, Integer, ArrayList<InstagramPost>> {
        GetInstagramData getData;

        @Override
        protected void onPreExecute(){
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<InstagramPost> doInBackground(String... params) {

            // Выбор стратегии доступа к Инстаграм
            if (!MainParameters.isUseAPI()) {
                //если работаем с web-интерфейсом
                getData = new GetInstagramDataByWeb();
            } else {
                //если работаем с API
                getData = new GetInstagramDataByApi();
            }
            publishProgress((int) 0);
            if (isCancelled()) {
                return null;
            }
            return getData.getTopPostList(params[0], maxPosts);
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(ArrayList<InstagramPost> result) {
            postList = result;
            adapter.addAll(postList);
            adapter.notifyDataSetChanged();
            if (result.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Такого пользователя не существует", Toast.LENGTH_LONG).show();
            }
            Toast.makeText(getApplicationContext(), "Данные загружены", Toast.LENGTH_LONG).show();

            makeCollage.setVisibility(View.VISIBLE);
            reportCount.setVisibility(View.GONE);
            bar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.makeCollage:
                doPhotoCollage(postList, 640, 640);
                break;
            case R.id.backButton:
                finish();
                break;
            default:
                //do something else
                break;
        }
    }

    /**
     * Генерация коллажа.
     * @param postList ArrayList объектов {@link InstagramPost}, отобранных в качестве лучших.
     *                 Содержат ссылки на файлы изображений.
     * @param width Ширина коллажа
     * @param height Высота коллажа
     */
    private void doPhotoCollage(ArrayList<InstagramPost> postList, int width, int height) {
        File smallImgFile;
        Bitmap bitmap;
        MakeCollage collageMaker = new MakeCollageByDecrisingRectangle();
        bitmap = collageMaker.getCollageBitmap(postList, width, height);
        smallImgFile = getOutputMediaFile();
        try {
            FileOutputStream out = new FileOutputStream(smallImgFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {}
        Toast.makeText(getApplicationContext(), "Collage complete", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getBaseContext(),CollageActivity.class);
        intent.putExtra("url", smallImgFile.getAbsolutePath());
        startActivity(intent);
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
                "COLLAGE_"+ timeStamp + Math.round(Math.random()*100) + ".jpg");
        return mediaFile;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(MainParameters.NETWORK_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
