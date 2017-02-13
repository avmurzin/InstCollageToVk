package com.avmurzin.instcollagetovk.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avmurzin.instcollagetovk.R;
import com.avmurzin.instcollagetovk.domain.MainParameters;

/**
 * Main Activity для ввода имени пользователя Instagram и переключения способа доступа (web или API).
 * Функциональность доступа через API ограничена одним пользователем (ограничение режима Sandbox
 * Instagram)).
 *
 * Created by Andrey V. Murzin (http://avmurzin.com) on 14.06.16.
 *
 * @author murzin
 * @version 0.1
 */
public class GetPhotoActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Button searchPhoto;
    private EditText username;
    private TextView error;
    private CheckBox useAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_photo);

        searchPhoto = (Button) findViewById(R.id.searchPhoto);
        username = (EditText) findViewById(R.id.username);
        useAPI = (CheckBox) findViewById(R.id.useAPI);
        error = (TextView) findViewById(R.id.error);

        searchPhoto.setOnClickListener(this);
        useAPI.setOnCheckedChangeListener(this);

        // Проверка наличия подключения к сети
        ConnectivityManager cm =
                (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            MainParameters.setIsOnline(true);
        } else {
            MainParameters.setIsOnline(false);
            Toast.makeText(this, "Отсутствует подключение к интернет", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.searchPhoto:
                doSearchPhoto();
                break;
            default:
                //do something else
                break;
        }

    }

    /**
     * Переключения способа доступа к Instagram.
     * @param buttonView
     * @param isChecked Флаг типа доступа (true - полный доступ через web; false - ограниченный доступ
     *                  через API)
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        MainParameters.setUseAPI(isChecked);
        if (isChecked) {
            username.setText("a.v.murzin");
            username.setEnabled(false);
            Toast.makeText(this, "Режим ограниченной функциональности", Toast.LENGTH_LONG).show();
        } else {
            username.setText("");
            username.setEnabled(true);
            Toast.makeText(this, "Режим полной функциональности", Toast.LENGTH_LONG).show();
        }
    }

    private void doSearchPhoto() {

        //for test only!
        //if (username.getText().length() == 0) {
        //    username.setText("a.v.murzin");
        //}

        if (username.getText().length() > 0) {
            if (MainParameters.getIsOnline()) {
                // Запуск процесса получения данных Instagram, в intent передается username Instagram
                MainParameters.setUsername(username.getText().toString());
                Intent intent = new Intent(getBaseContext(),PhotoPickerActivity.class);

                startActivity(intent);
            } else {
                Toast.makeText(this, "Отсутствует подключение к интернет!", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(this, "Ник пользователя не может быть пустым!", Toast.LENGTH_LONG).show();
        }
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
