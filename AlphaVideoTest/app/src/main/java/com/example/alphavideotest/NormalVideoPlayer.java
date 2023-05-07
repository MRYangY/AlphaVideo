package com.example.alphavideotest;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class NormalVideoPlayer implements SurfaceHolder.Callback, MediaPlayer.OnCompletionListener {
    private MediaPlayer mMediaPlayer;
    private String mFilePath = "demo_video.mp4";
    private SurfaceView mSurfaceView;
    private IPlayerListener mListener;


    public NormalVideoPlayer(SurfaceView surfaceView, IPlayerListener listener) {
        mSurfaceView = surfaceView;
        mListener = listener;
        mSurfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        try {
            mMediaPlayer = new MediaPlayer();
            AssetManager assetMan = mSurfaceView.getResources().getAssets();
            AssetFileDescriptor afd = assetMan.openFd(mFilePath);
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mMediaPlayer.setSurface(surfaceHolder.getSurface());
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setLooping(false);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mListener.onPlayStart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mListener != null) {
            mListener.onPlayFinish();
        }
    }
}
