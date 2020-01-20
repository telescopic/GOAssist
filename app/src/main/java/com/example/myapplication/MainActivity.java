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
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    ImageButton button,tempbtn,locbtn;
    Camera camera;
    FrameLayout frameLayout;
    ShowCamera showCamera;
    Bitmap picture;
    String picturePath;
    Animation anim1,anim2;
    LocationFinder locationFinder;

    static ArrayList<ArrayList<String>>arr=new ArrayList<ArrayList<String>>();
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
        locbtn=(ImageButton)findViewById(R.id.imageButton4);
        frameLayout=(FrameLayout) findViewById(R.id.frameLayout);
        //open the camera
        camera=Camera.open();
        showCamera=new ShowCamera(this,camera);
        frameLayout.addView(showCamera);

        //MODIFYING

        tempbtn=findViewById(R.id.imageButton5);
        arr.add(new ArrayList<String>(Arrays.asList("Aguada","15.4926","73.7732","Built by  Julião Simão in 1612","5 m high","Fort Aguada is a well-preserved seventeenth-century Portuguese fort, along with a lighthouse, standing in Goa, India, on Sinquerim Beach, overlooking the Arabian Sea.")));
        arr.add(new ArrayList<String>(Arrays.asList("Basilica of Bom Jesus","15.5009","73.9116","Built by  Julião Simão, Domingos Fernandes in 1605","18m high","The Basilica of Bom Jesus is a Roman Catholic basilica located in Goa, India, and is part of the Churches and convents of Goa UNESCO World Heritage Site. ")));
        arr.add(new ArrayList<String>(Arrays.asList("Shantadurga Temple","15.3961","73.9856","constructed during the reign of Maratha Empire ruler Chattrapati Shahu Maharaj of Satara","HEIGHT","Shri Shantadurga Temple is a Private temple complex belonging to Goud Saraswat Brahmin community. The temple is dedicated to Shantadurga, the goddess who mediates between Vishnu and Shiva. ")));

        tempbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("checking","enetered tempbtn");
                Intent intent =new Intent(MainActivity.this,Maps.class);
                intent.putExtra("lat","15.4926");
                intent.putExtra("long","73.7732");
                startActivity(intent);
            }
        });

//        locbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                locationFinder=new LocationFinder(MainActivity.this);
//                if(locationFinder.canGetLocation())
//                {
//                    double longi=locationFinder.getLongitude();
//                    double lat=locationFinder.getLatitude();
//                    double time=locationFinder.getTime();
//                    Toast.makeText(getApplicationContext(),"Latitude: "+Double.toString(lat)+"\nLongitde: "+Double.toString(longi),Toast.LENGTH_SHORT).show();
//                }
//                else
//                    locationFinder.showSettingsAlert();
//
//
//            }
//        });

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
