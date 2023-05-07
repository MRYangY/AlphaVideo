package com.example.alphavideotest;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.content_layout);
        findViewById(R.id.play_normal).setOnClickListener(v -> {
            SurfaceView surfaceView = new SurfaceView(this);
            new NormalVideoPlayer(surfaceView, new IPlayerListener() {
                @Override
                public void onPlayStart() {

                }

                @Override
                public void onPlayFinish() {
                    layout.removeView(surfaceView);
                }
            });
            layout.addView(surfaceView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        });
        findViewById(R.id.play_alpha).setOnClickListener(v -> {
            GLSurfaceView glSurfaceView = new GLSurfaceView(this);
            glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glSurfaceView.setZOrderOnTop(true);
            new AlphaVideoPlayer(glSurfaceView, new IPlayerListener() {
                @Override
                public void onPlayStart() {

                }

                @Override
                public void onPlayFinish() {
                    layout.removeView(glSurfaceView);
                }
            });
            layout.addView(glSurfaceView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        });
    }
}