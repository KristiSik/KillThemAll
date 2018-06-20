package com.edu4java.android.killthemall;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Star {
    private GameView gameView;
    private Bitmap bmp;
    private int x = 0;
    private int y = 0;
    private int width;
    private int height;
    private int xSpeed;
    private int ySpeed;
    private int currentFrame;
    private boolean visible = true;
    public Star(GameView gameView, Bitmap bmp, int initX, int initY, int xSpeed, int ySpeed) {
        this.gameView = gameView;
        this.bmp = bmp;
        this.height = bmp.getHeight();
        this.width = bmp.getWidth() / 3;
        x = initX;
        y = initY;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
    }
    private void update() {
        if (x >= gameView.getWidth() - width - xSpeed || x + xSpeed <= 0) {
            visible = false;
        }
        x = x + xSpeed;
        if (y >= gameView.getHeight() - height - ySpeed || y + ySpeed <= 0) {
            visible = false;
        }
        y = y + ySpeed;
        currentFrame = ++currentFrame % 3;
    }
    public void onDraw(Canvas canvas) {
        update();
        Rect src = new Rect(currentFrame * width, 0, (currentFrame + 1) * width, height);
        Rect dst = new Rect(x - width/2, y - height/2, x + width, y + height);
        canvas.drawBitmap(bmp, src, dst, null);
    }
    public boolean getVisible() {
        return visible;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
}
