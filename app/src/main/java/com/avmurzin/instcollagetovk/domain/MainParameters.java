package com.avmurzin.instcollagetovk.domain;

import android.app.Application;

import com.vk.sdk.VKSdk;

/**
 * Объект (расширяющий Application) для инициализации SDK Вконтакта и для хранения
 * глобальных параметров.
 *
 * Created by Andrey V. Murzin (http://avmurzin.com) on 14.06.16.
 *
 * @author murzin
 * @version 0.1
 */
public class MainParameters extends Application {

    /**
     * Имя action для бродкастов
     */
    public static final String NETWORK_ACTION = "instcollagetovk.networkaction";

    /**
     * Папка для временного хранения изображений
     */
    public static final String TMP_IMAGE_FOLDER = "TemporaryInstCollageToVkImages";

    /**
     * Индикатор наличия доступа в сеть
     */
    private static boolean isOnline;

    /**
     * Имя пользователя Инстаграм
     */
    private static String username;

    /**
     * Флаг типа стратегии доступа в Инстаграм
     */
    private static boolean useAPI;

    public synchronized static void setIsOnline(boolean status) {
        isOnline = status;
    }
    public static boolean getIsOnline() {
        return isOnline;
    }

    public static String getUsername() {
        return username;
    }

    public synchronized static void setUsername(String login) {
        username = login;
    }

    public static boolean isUseAPI() {
        return useAPI;
    }

    public synchronized static void setUseAPI(boolean useAPI) {
        MainParameters.useAPI = useAPI;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }
}
