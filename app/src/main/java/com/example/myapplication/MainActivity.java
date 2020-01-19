package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.animation.*;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    ImageButton button;
    Camera camera;
    FrameLayout frameLayout;
    ShowCamera showCamera;
    Bitmap picture;
    String picturePath;
    Animation anim1,anim2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        anim1=AnimationUtils.loadAnimation(this,R.anim.button_zoom_in);
        anim2=AnimationUtils.loadAnimation(this,R.anim.button_zoom_out);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar
        button= (ImageButton) findViewById(R.id.capture);
        frameLayout=(FrameLayout) findViewById(R.id.frameLayout);
        //open the camera
        camera=Camera.open();
        showCamera=new ShowCamera(this,camera);
        frameLayout.addView(showCamera);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //camera.startPreview();
        //frameLayout.addView(showCamera);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //camera.stopPreview();
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            picture= BitmapFactory.decodeByteArray(data,0,data.length);
            picturePath=saveToInternalStorage(picture);
            openDisplayPicture();
            /*
            File picture_file = getOutputMediaFile();
            if(picture_file==null) return;
            try {
                FileOutputStream fos = new FileOutputStream(picture_file);
                fos.write(data);
                fos.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }*/

        }
    };


    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    public void captureImage(View v){
        button.startAnimation(anim1);
        button.startAnimation(anim2);
        if(camera!=null){
            camera.takePicture(null,null,mPictureCallback);
        }
    }

    public void openDisplayPicture(){
        Intent intent = new Intent(this, DisplayPicture.class);
        intent.putExtra("path",picturePath);
        startActivity(intent);

    }
}
