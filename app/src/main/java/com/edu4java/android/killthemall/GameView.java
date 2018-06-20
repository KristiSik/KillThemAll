package com.edu4java.android.killthemall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

class GameView extends SurfaceView {
    private final SurfaceHolder holder;
    private final Bitmap bmpBlood;
    private GameLoopThread gameLoopThread;
    private List<Sprite> sprites = new ArrayList<Sprite>();
    private Sprite goodGuy;
    private List<TempSprite> temps = new ArrayList<TempSprite>();
    private long lastClick;
    private boolean gameOver = false;
    private boolean allSpritesAdded = false;
    private Star star;

    public GameView(Context context) {
        super(context);
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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        goodGuy.setSpeed(event.getX(), event.getY());
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.star);
        star = new Star(this, bmp, goodGuy.getX(), goodGuy.getY(), goodGuy.getXSpeed()*3, goodGuy.getYSpeed()*3);
        return true;
    }

    private void createSprites() {
        sprites.add(createSprite(R.drawable.bad1, false));
        sprites.add(createSprite(R.drawable.bad2, false));
        sprites.add(createSprite(R.drawable.bad3, false));
        sprites.add(createSprite(R.drawable.bad4, false));
        sprites.add(createSprite(R.drawable.bad5, false));
        sprites.add(createSprite(R.drawable.bad6, false));
        goodGuy = createSprite(R.drawable.good1, true);
        sprites.add(goodGuy);
        // sprites.add(createSprite(R.drawable.good2, true));
        // sprites.add(createSprite(R.drawable.good3, true));
        // sprites.add(createSprite(R.drawable.good4, true));
        // sprites.add(createSprite(R.drawable.good5, true));
        // sprites.add(createSprite(R.drawable.good6, true));
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
        for (Sprite spriteA : sprites) {
            if (spriteA.goodOne) {
                for (Sprite spriteB : sprites) {
                    if (!spriteB.goodOne) {
                        if (spriteA.isCollition(spriteB.getX(), spriteB.getY())) {
                            spritesToRemove.add(spriteA);
                            spritesToRemove.add(spriteB);
                        }
                    }
                }
            }
        }
        if (star != null && star.getVisible()) {
            for (Sprite spriteB : sprites) {
                if (!spriteB.goodOne) {
                    if (spriteB.isCollition(star.getX(), star.getY())) {
                        spritesToRemove.add(spriteB);
                        star = null;
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
                String winner;
                if (goodSpriteNo == 0) {
                    winner = "Bad";
                } else {
                    winner = "Good";
                }
                displayGameOverMessage(canvas, winner);
            }
        }
    }
    protected void displayGameOverMessage(Canvas canvas, String winner) {
        Typeface tf = Typeface.create("Helvetica",Typeface.BOLD);
        Paint paint = new Paint();
        paint.setTypeface(tf);
        if (winner == "Bad") {
            paint.setColor(Color.RED);
        } else {
            paint.setColor(Color.GREEN);
        }
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(40);
        canvas.drawText("The winner are " + winner + " Guys", canvas.getWidth()/2, canvas.getHeight()/2, paint);
    }
}
