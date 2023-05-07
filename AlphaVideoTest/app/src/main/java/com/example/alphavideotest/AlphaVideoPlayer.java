package com.example.alphavideotest;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AlphaVideoPlayer implements GLSurfaceView.Renderer, MediaPlayer.OnCompletionListener, SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "AlphaVideoPlayer";
    private String mFilePath = "demo_video.mp4";
    private SurfaceTexture mSurfaceTexture;
    private int mTexture;
    private int glProgId = -1;
    private int glUniformMatrix;
    private int glAttribPosition;
    private int glAttribInputTextureCoordinate;
    private int glUniformTexture;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private boolean updateSurface = false;
    private final float[] mMatrix = new float[16];
    private final FloatBuffer glCubeBuffer;
    private final FloatBuffer glTextureBuffer;
    private GLSurfaceView mGLSurfaceView;
    private MediaPlayer mMediaPlayer;
    private IPlayerListener mListener;

    public AlphaVideoPlayer(GLSurfaceView gLSurfaceView, IPlayerListener listener) {
        this.mGLSurfaceView = gLSurfaceView;
        this.mListener = listener;
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        glCubeBuffer = ByteBuffer.allocateDirect(OpenGlUtils.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        glCubeBuffer.put(OpenGlUtils.CUBE).position(0);

        glTextureBuffer = ByteBuffer.allocateDirect(OpenGlUtils.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        glTextureBuffer.put(OpenGlUtils.TEXTURE_NO_ROTATION).position(0);
        Matrix.setIdentityM(mMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        glProgId = OpenGlUtils.loadProgram(OpenGlUtils.VERTEX_SHADER, OpenGlUtils.ALPHA_BLEND_FRAGMENT_SHADER);
        OpenGlUtils.checkGlError("loadProgram");
        glUniformMatrix = GLES20.glGetUniformLocation(glProgId, "surfaceTransformMatrix");
        OpenGlUtils.checkGlError("glGetUniformLocation surfaceTransformMatrix");
        glAttribPosition = GLES20.glGetAttribLocation(glProgId, "position");
        OpenGlUtils.checkGlError("glGetUniformLocation position");
        glAttribInputTextureCoordinate = GLES20.glGetAttribLocation(glProgId, "inputTextureCoordinate");
        OpenGlUtils.checkGlError("glGetUniformLocation inputTextureCoordinate");
        glUniformTexture = GLES20.glGetUniformLocation(glProgId, "inputImageTexture");
        OpenGlUtils.checkGlError("glGetUniformLocation inputImageTexture");
        Log.i(TAG, "--onSurfaceCreated--glProgId : " + glProgId + "--" + glUniformMatrix + "--" + glAttribPosition + "--" + glAttribInputTextureCoordinate + "--" + glUniformTexture);
        mTexture = OpenGlUtils.createOESTexture();
        OpenGlUtils.checkGlError("createTexture");
        Log.i(TAG, "--onSurfaceCreated--texture : " + mTexture);
        mSurfaceTexture = new SurfaceTexture(mTexture);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        Surface surface = new Surface(mSurfaceTexture);
        configPlayer(surface);
        surface.release();
        Log.i(TAG, "--onSurfaceCreated--finish");
    }

    private void configPlayer(Surface surface) {
        try {
            mMediaPlayer = new MediaPlayer();
            AssetManager assetMan = mGLSurfaceView.getResources().getAssets();
            AssetFileDescriptor afd = assetMan.openFd(mFilePath);
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mMediaPlayer.setSurface(surface);
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
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        mSurfaceWidth = i;
        mSurfaceHeight = i1;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        Log.i(TAG, "--onDrawFrame--");
        synchronized (this) {
            if (updateSurface && mSurfaceTexture != null) {
                mSurfaceTexture.updateTexImage();
                mSurfaceTexture.getTransformMatrix(mMatrix);
                updateSurface = false;
            }
        }
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        GLES20.glUseProgram(glProgId);
        OpenGlUtils.checkGlError("glUseProgram");
        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 0, glCubeBuffer);
        GLES20.glEnableVertexAttribArray(glAttribPosition);
        GLES20.glVertexAttribPointer(glAttribInputTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, glTextureBuffer);
        GLES20.glEnableVertexAttribArray(glAttribInputTextureCoordinate);
        OpenGlUtils.checkGlError("1111");

        GLES20.glUniformMatrix4fv(glUniformMatrix, 1, false, mMatrix, 0);
        OpenGlUtils.checkGlError("2222");
        if (mTexture != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTexture);
            GLES20.glUniform1i(glUniformTexture, 0);
        }
        OpenGlUtils.checkGlError("3333");
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        OpenGlUtils.checkGlError("4444");
        GLES20.glDisableVertexAttribArray(glAttribPosition);
        GLES20.glDisableVertexAttribArray(glAttribInputTextureCoordinate);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glFinish();
        OpenGlUtils.checkGlError("5555");
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            updateSurface = true;
            mGLSurfaceView.requestRender();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mListener.onPlayFinish();
    }
}
