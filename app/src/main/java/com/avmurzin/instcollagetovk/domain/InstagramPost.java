package com.avmurzin.instcollagetovk.domain;

/**
 * Объект, содержащий данные о посте Инстаграм.
 *
 * Created by Andrey V. Murzin (http://avmurzin.com) on 14.06.16.
 *
 * @author murzin
 * @version 0.1
 */
public class InstagramPost {
    /**
     * Ссылка на изображение в интернет
     */
    private String url;
    /**
     * Идентификатор поста
     */
    private String id;
    /**
     * Число лайков
     */
    private int likes;
    /**
     * Ссылка на локально сохраненное изображение
     */
    private String localPath;
    /**
     * Дата публикации
     */
    private String date;
    /**
     * Заголовок
     */
    private String title;
    /**
     * Выбран для создания коллажа
     */
    private boolean isSelected;
    /**
     * Ширина изображения
     */
    private int width;
    /**
     * Высота изображения
     */
    private int height;

    public InstagramPost() {
        this.isSelected = false;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String toString() {
        return url + ":" + id + ":" + likes + ":" + localPath + ":" + date + ":" + title + ":" + isSelected + ":" + width + ":" + height + ";";
    }
}
