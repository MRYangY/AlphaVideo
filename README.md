# AlphaVideo
play alpha video in Android







## Android平台播放透明视频

### 思路

设计一种特殊的视频，它的一半内容存储alpha信息，另一半内容存储rgb信息，接着通过OpenGL获取每个像素点的alpha值和rgb值进行混合，最后出来的画面就是带有透明效果的视频了。

可以上下的分，也可以左右的分，区别在于glsl的写法。



![上下分](https://raw.githubusercontent.com/MRYangY/blog-img/main/alpha1.webp)



![左右分](https://raw.githubusercontent.com/MRYangY/blog-img/main/alpha2.png)



演示demo中的视频素材采用左右分的方式。

https://raw.githubusercontent.com/MRYangY/blog-img/main/demo_video.mp4



### 效果

![播放视频-处理前](https://raw.githubusercontent.com/MRYangY/blog-img/main/test-normal22.gif)



![播放视频-处理后](https://raw.githubusercontent.com/MRYangY/blog-img/main/test-alpha.gif)



### 开发

主要用到MediaPlayer、SurfaceTexture、GLSurfaceview。

利用MediaPlayer配合SurfaceTexture，把视频画面转成纹理，作为输入，通过OpenGL渲染管线处理，最终画到GLSurfaceView上。



核心代码如下：

利用SurfaceTexture接收视频帧，方便二次处理。

```java
mSurfaceTexture = new SurfaceTexture(mTexture);
mSurfaceTexture.setOnFrameAvailableListener(this);
Surface surface = new Surface(mSurfaceTexture);
...
mMediaPlayer.setSurface(surface);
...
```

shader代码(视频左边是alpha信息，右边的rgb信息)

```java
public static final String VERTEX_SHADER = "uniform mat4 surfaceTransformMatrix;\n" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            " \n" +
            "varying vec2 rgbTextureCoordinate;\n" +
            "varying vec2 alphaTextureCoordinate;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    float rgbXOrigin = 0.5;\n" +
            "    float alphaXOrigin = 0.0;\n" +
            "    float channelScale = 2.0;\n" +
            "\n" +
            "    float rgbX = inputTextureCoordinate.x / channelScale + rgbXOrigin;\n" +
            "    float alphaX = inputTextureCoordinate.x / channelScale + alphaXOrigin;\n" +
            "\n" +
            "    vec4 positionInRgbTexture = vec4(rgbX, inputTextureCoordinate.y, inputTextureCoordinate.zw);\n" +
            "    vec4 positionInAlphaTexture = vec4(alphaX, inputTextureCoordinate.y , inputTextureCoordinate.zw);\n" +
            "\n" +
            "    rgbTextureCoordinate = (surfaceTransformMatrix * positionInRgbTexture).xy;\n" +
            "    alphaTextureCoordinate = (surfaceTransformMatrix * positionInAlphaTexture).xy;\n" +
            "}";

    public static final String ALPHA_BLEND_FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 rgbTextureCoordinate;\n" +
            "varying vec2 alphaTextureCoordinate;\n" +
            "\n" +
            " uniform samplerExternalOES inputImageTexture;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "   vec4 rgbColor = texture2D(inputImageTexture, rgbTextureCoordinate);\n" +
            "   float alphaColor = texture2D(inputImageTexture, alphaTextureCoordinate).g;\n" +
            "\n" +
            "   gl_FragColor = vec4(rgbColor.rgb * alphaColor, alphaColor);\n" +
            " }";
```



[DemoProjectLink]: https://github.com/MRYangY/AlphaVideo	"AlphaVideoDemo"



## 结语

1. 标题虽然说是Android平台播放透明视频，但是因为是用OpenGL来做的，其他平台也可以用这个思路和shader来实现。
2. 虽然透明视频效果出来了，但是应该发现，会有变形。这是因为存在比例不一致导致的，需要修改顶点坐标或者viewport，后面会再写篇文章细说。

