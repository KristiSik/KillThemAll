package com.edu4java.android.killthemall;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;

public class GameSound {
    public static final int BAD_GUY_KILLED = 1;
    public static final int GOOD_GUY_KILLED = 2;
    public static final int THROWN_SHURIKEN = 3;

    private SoundPool soundPool;
    private SoundPool.Builder soundPoolBuilder;
    private AudioAttributes attributes;
    private AudioAttributes.Builder attributesBuilder;
    private int soundBadGuyKilled;
    private int soundGoodGuyKilled;
    private int soundThrownShuriken;


    public GameSound(Context context) {
        createSoundLoop();
        loadSound(context);
    }

    private void createSoundLoop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            attributesBuilder = new AudioAttributes.Builder();
            attributesBuilder.setUsage(AudioAttributes.USAGE_GAME);
            attributesBuilder.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
            attributes = attributesBuilder.build();
            soundPoolBuilder = new SoundPool.Builder();
            soundPoolBuilder.setAudioAttributes(attributes);
            soundPool = soundPoolBuilder.build();
        }
    }

    private void loadSound(Context context) {
        soundBadGuyKilled = soundPool.load(context, R.raw.rebel, 1);
        soundGoodGuyKilled = soundPool.load(context, R.raw.groan, 1);
        soundThrownShuriken = soundPool.load(context, R.raw.shuriken, 1);
    }

    public void playSound(int soundID) {
        switch (soundID) {
            case BAD_GUY_KILLED:
                soundPool.play(soundBadGuyKilled, 1, 1, 1, 0,1);
                break;
            case GOOD_GUY_KILLED:
                soundPool.play(soundGoodGuyKilled, 1, 1, 1, 0,1);
                break;
            case THROWN_SHURIKEN:
                soundPool.play(soundThrownShuriken, 1, 1, 1, 0,1);
                break;
        }
    }
}
