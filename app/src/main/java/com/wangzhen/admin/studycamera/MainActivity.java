package com.wangzhen.admin.studycamera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private Button button;
    private SurfaceView surfaceView;
    private ImageView imageView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.bt_take_photo);
        imageView = findViewById(R.id.image);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(camera != null){
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                            Matrix matrix = new Matrix();
                            matrix.postRotate(90);
                            Bitmap bitmap1 = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth() , bitmap.getHeight(),matrix,true);
                            surfaceView.setVisibility(View.GONE);
                            imageView.setImageBitmap(bitmap1);
                            imageView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        surfaceView = findViewById(R.id.surfaceView);



        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();
        Log.i("MDL", "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        /*try {
        //    camera.setPreviewDisplay(holder);
         //   camera.startPreview();
            Log.i("MDL", "surfaceChanged");
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        Log.i("MDL", "surfaceDestroyed");
    }

    //打开相机
    private void openCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }
        camera = Camera.open();
        //0 代表后置摄像头
        setCameraDisplayOrientation(this,0,camera);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();//获取所有支持的camera尺寸
        Log.d("jxd","optionSize : mSurfaceView "+ surfaceView.getWidth()+" * "+ surfaceView.getHeight());
        Camera.Size optionSize = getOptimalPreviewSize(sizeList, surfaceView.getHeight(), surfaceView.getWidth());//获取一个最为适配的camera.size
        Log.d("jxd","optionSize : "+optionSize.width+" * "+optionSize.height);
        parameters.setPreviewSize(optionSize.width,optionSize.height);//把camera.size赋值到parameters
        camera.setParameters(parameters);
        //通过SurfaceView显示预览
        try {
            camera.setPreviewDisplay(surfaceHolder);
            //开始预览
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * orientation表示相机图像的方向。它的值是相机图像顺时针旋转到设备自然方向一致时的角度。
     * 例如假设设备是竖屏的。后置相机传感器是横屏安装的。
     * 当你面向屏幕时，如果后置相机传感器顶边的和设备自然方向的右边是平行的，则后置相机的orientation是90。
     * 如果前置相机传感器顶边和设备自然方向的右边是平行的，则前置相机的orientation是270。
     *
     * @param activity 相机显示在的Activity
     * @param cameraId 相机的ID
     * @param camera 相机对象
     */
    public void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera)
    {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation)
        {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        }
        else
        {
            // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

}
