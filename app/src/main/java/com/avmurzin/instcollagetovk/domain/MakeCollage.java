package com.avmurzin.instcollagetovk.domain;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Интерфейс к стратегиям генерации коллажа.
 *
 * Created by Andrey V. Murzin (http://avmurzin.com) on 14.06.16.
 *
 * @author murzin
 * @version 0.1
 */
public interface MakeCollage {
    /**
     * Получить коллаж из набора изображений.
     * @param postList Список объектов {@link InstagramPost}, загруженных в качестве лучших. В коллаж
     *                 попадают те, что имеют установленный признак {@link InstagramPost#isSelected()}
     * @param imageWidth Ширина коллажа
     * @param imageHeight Высота коллажа
     * @return
     */
    public Bitmap getCollageBitmap(ArrayList<InstagramPost> postList, int imageWidth, int imageHeight);
}
