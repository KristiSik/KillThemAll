package com.edu4java.android.killthemall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class GameView extends SurfaceView {
    private final SurfaceHolder holder;
    private final Bitmap bmpBlood;
    private final Bitmap bmpStar;
    private GameLoopThread gameLoopThread;
    private GameSound gameSound;
    private List<Sprite> sprites;
    private Sprite goodGuy;
    private List<TempSprite> temps;
    private long lastClick;
    private boolean gameOver = false;
    private boolean allSpritesAdded = false;
    private Star star;
    private long timeGameStarted;


    public GameView(Context context) {
        super(context);
        gameSound = new GameSound(context);
        gameLoopThread = new GameLoopThread(this);
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                gameLoopThread.setRunning(true);
                gameLoopThread.start();
                createSprites();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

        });
        bmpBlood = BitmapFactory.decodeResource(getResources(), R.drawable.blood1);
        bmpStar = BitmapFactory.decodeResource(getResources(), R.drawable.star);
        timeGameStarted = System.currentTimeMillis();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOver && event.getAction() == MotionEvent.ACTION_DOWN) {
            allSpritesAdded = false;
            gameLoopThread = new GameLoopThread(this);
            gameLoopThread.setRunning(true);
            gameLoopThread.start();
            createSprites();
            gameOver = false;
            timeGameStarted = System.currentTimeMillis();
        }
        if (allSpritesAdded) {
            goodGuy.setSpeed(event.getX(), event.getY());
        }
        if (event.getAction() == MotionEvent.ACTION_UP && event.getEventTime() - event.getDownTime() <= 500) {
            star = new Star(this, bmpStar, goodGuy.getX(), goodGuy.getY(), goodGuy.getXSpeed()*3, goodGuy.getYSpeed()*3);
            gameSound.playSound(GameSound.THROWN_SHURIKEN);
        }
        return true;
    }

    private void createSprites() {
        int[] badGuyResources = {R.drawable.bad1, R.drawable.bad2, R.drawable.bad3, R.drawable.bad4, R.drawable.bad5, R.drawable.bad6};
        int[] goodGuyResources = {R.drawable.good1, R.drawable.good2, R.drawable.good3, R.drawable.good4, R.drawable.good5, R.drawable.good6};
        sprites = new ArrayList<>();
        temps = new ArrayList<>();
        for(int resource : badGuyResources) {
            sprites.add(createSprite(resource, false));
        }
        goodGuy = createSprite(goodGuyResources[new Random().nextInt(goodGuyResources.length - 1)], true);
        sprites.add(goodGuy);
        allSpritesAdded = true;
    }

    private boolean isCollision(Rect sprite1, Rect sprite2) {
        if (System.currentTimeMillis() - timeGameStarted >= 400)
        {
            return sprite1.intersect(sprite2);
        } else {
            return false;
        }
    }

    private Sprite createSprite(int resource, boolean good) {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), resource);
        return new Sprite(this, bmp, good);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas != null && allSpritesAdded) {
            canvas.drawColor(Color.BLACK);
            if (star != null && star.getVisible())
                star.onDraw(canvas);

            List<Sprite> spritesToRemove = new ArrayList<Sprite>();
            for (Sprite spriteB : sprites) {
                if (!spriteB.goodOne) {
                     if (isCollision(goodGuy.getRect(), spriteB.getRect())) {
                        spritesToRemove.add(goodGuy);
                        gameSound.playSound(GameSound.GOOD_GUY_KILLED);
                        spritesToRemove.add(spriteB);
                    }
                }
            }
            if (star != null && star.getVisible()) {
                for (Sprite spriteB : sprites) {
                    if (!spriteB.goodOne) {
                        if (isCollision(spriteB.getRect(), star.getRect())) {
                            spritesToRemove.add(spriteB);
                            star = null;
                            gameSound.playSound(GameSound.BAD_GUY_KILLED);
                            break;
                        }
                    }
                }
            }
            for (Sprite sprite : spritesToRemove) {
                temps.add(new TempSprite(temps, this, sprite.getX(), sprite.getY(), bmpBlood));
                sprites.remove(sprite);
            }

            int goodSpriteNo = 0;
            int badSpriteNo = 0;
            for (int i = temps.size() - 1; i >= 0; i--) {
                temps.get(i).onDraw(canvas);
            }
            for(Sprite sprite : sprites) {
                sprite.onDraw(canvas);
                if (sprite.goodOne) {
                    goodSpriteNo++;
                } else {
                    badSpriteNo++;
                }
            }
            if (goodSpriteNo == 0 || badSpriteNo == 0) {
                gameOver = true;
                gameLoopThread.setRunning(false);
                boolean goodWins = true;
                if (goodSpriteNo == 0) {
                    goodWins = false;
                }
                displayGameOverMessage(canvas, goodWins);
            }
        }
    }
    protected void displayGameOverMessage(Canvas canvas, boolean goodWins) {
        Typeface tf = Typeface.create("Helvetica",Typeface.BOLD);
        Paint paint = new Paint();
        paint.setTypeface(tf);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(40);
        if (!goodWins) {
            paint.setColor(Color.RED);
            canvas.drawText("The winner are Bad Guys", canvas.getWidth()/2, canvas.getHeight()/3, paint);
        } else {
            paint.setColor(Color.GREEN);
            canvas.drawText("The winner is Good Guy", canvas.getWidth()/2, canvas.getHeight()/3, paint);
        }
    }
}
