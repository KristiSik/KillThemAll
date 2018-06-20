package com.edu4java.android.killthemall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

class GameView extends SurfaceView {
    private final SurfaceHolder holder;
    private final Bitmap bmpBlood;
    private final Bitmap bmpStar;
    private GameLoopThread gameLoopThread;
    private GameSound gameSound;
    private List<Sprite> sprites = new ArrayList<Sprite>();
    private Sprite goodGuy;
    private List<TempSprite> temps = new ArrayList<TempSprite>();
    private long lastClick;
    private boolean gameOver = false;
    private boolean allSpritesAdded = false;
    private Star star;

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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        goodGuy.setSpeed(event.getX(), event.getY());
        if (event.getAction() == MotionEvent.ACTION_UP && event.getEventTime() - event.getDownTime() <= 500) {
            star = new Star(this, bmpStar, goodGuy.getX(), goodGuy.getY(), goodGuy.getXSpeed()*3, goodGuy.getYSpeed()*3);
            gameSound.playSound(GameSound.THROWN_SHURIKEN);
        }
        return true;
    }

    private void createSprites() {
        int[] badGuyResources = {R.drawable.bad1, R.drawable.bad2, R.drawable.bad3, R.drawable.bad4, R.drawable.bad5, R.drawable.bad6};
        for(int resource : badGuyResources) {
            sprites.add(createSprite(resource, false));
        }
        goodGuy = createSprite(R.drawable.good1, true);
        sprites.add(goodGuy);
        allSpritesAdded = true;
    }

    private Sprite createSprite(int resource, boolean good) {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), resource);
        return new Sprite(this, bmp, good);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);

        if (star != null && star.getVisible())
            star.onDraw(canvas);

        List<Sprite> spritesToRemove = new ArrayList<Sprite>();
        for (Sprite spriteB : sprites) {
            if (!spriteB.goodOne) {
                if (goodGuy.isCollition(spriteB.getX(), spriteB.getY())) {
                    spritesToRemove.add(goodGuy);
                    gameSound.playSound(GameSound.GOOD_GUY_KILLED);
                    spritesToRemove.add(spriteB);
                }
            }
        }
        if (star != null && star.getVisible()) {
            for (Sprite spriteB : sprites) {
                if (!spriteB.goodOne) {
                    if (spriteB.isCollition(star.getX(), star.getY())) {
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
        if (allSpritesAdded) {
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
