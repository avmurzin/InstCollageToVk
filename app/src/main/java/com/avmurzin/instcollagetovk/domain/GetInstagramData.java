package com.avmurzin.instcollagetovk.domain;

import java.util.ArrayList;

/**
 * Интерфейс к стратегиям загрузки данных из Инстаграм.
 *
 * Created by Andrey V. Murzin (http://avmurzin.com) on 14.06.16.
 *
 * @author murzin
 * @version 0.1
 */
public interface GetInstagramData {
    /**
     * Получить список лучших постов (из всего множества постов указанного пользователя выбираются
     * те, что имеют больше лайков).
     * @param username Имя пользователя Инстаграм, лучшие картинки которого будут загружены
     * @param listSize Размер top-листа (т.е. выбирается listSize лучших)
     * @return Список объектов {@link InstagramPost}, содержащих данные о лучших постах и ссылки
     * на загруженные картинки
     */
    public ArrayList<InstagramPost> getTopPostList(String username, int listSize);

    /**
     * Получить текущий счетчик обработанных постов.
     * @deprecated
     * @return
     */
    public int getPostCount();
}
