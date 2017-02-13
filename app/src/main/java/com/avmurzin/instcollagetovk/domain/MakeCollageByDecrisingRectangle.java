package com.avmurzin.instcollagetovk.domain;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;

/**
 * Реализация интерфейса {@link MakeCollage} алгоритма создания коллажа. Исходный прямоугольник
 * в случайной пропорции делится на две части (случайно по вертикали или горизонтали). В первую
 * загружается соотв. кусок первого изображения. Вторая часть снова делится случайно на две части.
 * И далее в цикле.
 *
 * Created by Andrey V. Murzin (http://avmurzin.com) on 14.06.16.
 *
 *@author murzin
 * @version 0.1
 */
public class MakeCollageByDecrisingRectangle implements MakeCollage {
    @Override
    public Bitmap getCollageBitmap(ArrayList<InstagramPost> postList, int width, int height) {
        InstagramPost post;
        int x = 0;
        int y = 0;
        Bitmap collage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Bitmap imagePart, garbage;
        Canvas canvas = new Canvas(collage);
        Paint paint = new Paint();

        boolean direction = true;

        if (Math.random() >= 0.5) {
            direction = false;
        }

        for (int i = 0; i < postList.size(); i++) {
            post = postList.get(i);
            if (post.isSelected()) {
                garbage = BitmapFactory.decodeFile(post.getLocalPath());
                if ((garbage.getWidth() >= (width - x)) && (garbage.getHeight() >= (height - y))) {
                    imagePart = Bitmap.createBitmap(garbage, x, y, width - x, height - y);
                } else {
                    imagePart = Bitmap.createBitmap(garbage, x, y, garbage.getWidth() - x, garbage.getHeight() - y);
                }

                canvas.drawBitmap(imagePart, x, y, paint);
                if (direction) {
                    x += (int) (Math.random() * (width - x - 1));
                } else {
                    y += (int) (Math.random() * (height - y - 1));
                }
                direction = !direction;
                Log.e("MakeCollage", "x=" + x + "  " + "y=" + y);
            }
        }
        return collage;
    }
}
