package com.avmurzin.instcollagetovk.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.avmurzin.instcollagetovk.domain.MainParameters;

/**
 * Ресивер событий подключенения/отклчения сетевого соединения.В соответствии с изменившимся
 * состоянием меняется флаг {@link MainParameters#getIsOnline()}
 *
 * Created by Andrey V. Murzin (http://avmurzin.com) on 14.06.16.
 *
 *@author murzin
 * @version 0.1
 */
public class NetworkTrack extends BroadcastReceiver {
    private static final String ACTION_NAME = "android.net.conn.CONNECTIVITY_CHANGE";
    private Context context;

    public NetworkTrack() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if ((intent != null)
                && (intent.getAction() != null)
                && (intent.getAction().compareToIgnoreCase(ACTION_NAME) == 0)) {

            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                MainParameters.setIsOnline(true);
            } else {
                MainParameters.setIsOnline(false);
            }

        }
    }
}
