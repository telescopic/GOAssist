package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;

import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

class TempClass{
    Point from;
    Point to;
    Canvas canvas;
    Paint paint;
}

class LineAnimation extends AsyncTask<TempClass,Void,Void>{
    @Override
    protected Void doInBackground(TempClass... tempClasses) {
        TempClass t=tempClasses[0];
        float dX=t.to.x-t.from.x;
        float dY=t.to.y-t.from.y;
        for(float v=0;v<=1.0;v+=0.05) {
            t.canvas.drawLine(t.from.x,t.from.y,t.from.x + v * dX, t.from.y + v * dY, t.paint);
            //t.canvas.drawLine(t.from.x, t.from.y, t.from.x + v * dX, t.from.y + v * dY, t.paint);
        }
        return null;
    }
}

public class DisplayPicture extends AppCompatActivity {
    Bitmap picture;
    ImageView imageView;
    ImageView cImage;
    String picturePath;
    EditText description;
    RelativeLayout relativeLayout;
    CustomView cv;

    ImageButton landmarkDetected;
    ImageButton []infoImageButtons;
    ImageButton []nearbyplaceImageButton;

    Point []fromPoint;
    Point []toPoint;

    AnimationSet as;

    Canvas mCanvas;
    Paint p;

    int []buttonX;
    int []buttonY;
    int number_of_buttons;
    int button_state; // stores the state that the button is currently in:
    // 0- unclicked state
    // 1- information display state
    // 2- nearby location display state
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Miscellaneous crap

        //set initial button state
        button_state=0;

        //initialize the array of points
        fromPoint=new Point[3];
        toPoint=new Point[3];
        initPoints();

        //number of info and nearby place buttons
        number_of_buttons=3;
        infoImageButtons=new ImageButton[number_of_buttons];
        buttonX=new int[number_of_buttons];
        buttonY=new int[number_of_buttons];
        initializeButtonXY();

        // Hiding title bar and making it fullscreen
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_display_picture);
        getSupportActionBar().hide(); //hide the title bar


        //set canvas image to something transparent
        cImage=(ImageView)findViewById(R.id.cImage);
        Bitmap mBackgroundImage = Bitmap.createBitmap(1000, 1000,
                Bitmap.Config.ARGB_4444);
        cImage.setBackgroundDrawable(new BitmapDrawable(mBackgroundImage));
        mCanvas=new Canvas(mBackgroundImage);
        p=new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStrokeWidth(4);
        p.setColor(Color.WHITE);

        // Retreiving image from internal storage
        picturePath=getIntent().getStringExtra("path");
        loadImageFromStorage(picturePath);

        // Caching the relative layout variable for adding new buttons dynamically
        relativeLayout=(RelativeLayout) findViewById(R.id.relativeLayout);

        //// DEBUGGING PORTION
        onLandmarkDetected();
    }

    public void initPoints(){
        fromPoint[0]=new Point(560,470); toPoint[0]=new Point(790,320);
        fromPoint[1]=new Point(420,500); toPoint[1]=new Point(230,500);
        fromPoint[2]=new Point(560,530); toPoint[2]=new Point(790,650);
    }

    public void initializeButtonXY(){
        buttonX[0]=50; buttonY[0]=800;
        buttonX[1]=750; buttonY[1]=400;
        buttonX[2]=750; buttonY[2]=1200;
    }

    public void addInfoButton(){
        foo();
//        cv=new CustomView(this); cv.paint.setColor(Color.GREEN);
//        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(500,500);
//        cv.setLayoutParams(params);
//        relativeLayout.addView(cv);
//        cv.init();
        Animation animation=AnimationUtils.loadAnimation(this,R.anim.fade_in);
        for(int i=0;i<number_of_buttons;i++){
            RelativeLayout.LayoutParams param=new RelativeLayout.LayoutParams(220, 200);
            param.leftMargin=buttonX[i];
            param.topMargin=buttonY[i];
            infoImageButtons[i]=new ImageButton(this);
            infoImageButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateDescription("Zoo");
                }
            });
            infoImageButtons[i].setBackgroundResource(R.drawable.height_icon);
            infoImageButtons[i].setLayoutParams(param);;

//            // Line drawing section
            TempClass temp=new TempClass();
            temp.canvas=mCanvas;
            temp.paint=p;
            temp.paint.setColor(Color.WHITE);
            temp.paint.setXfermode(null);
            temp.from=fromPoint[i];
            temp.to=toPoint[i];
            new LineAnimation().execute(temp);
            //
            relativeLayout.addView(infoImageButtons[i]);


            infoImageButtons[i].startAnimation(animation);
        }
    }

    public void deleteInfoButton(){
//        cv.rinit();
        Animation animation=AnimationUtils.loadAnimation(this,R.anim.fade_out);
        for(int i=0;i<number_of_buttons;i++){
            ImageButton T = infoImageButtons[i];

//            // Line drawing section
            TempClass temp=new TempClass();
            temp.canvas=mCanvas;
            temp.paint=p;
            temp.paint.setStrokeWidth(6);
            temp.paint.setColor(Color.TRANSPARENT);
            temp.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            temp.from=fromPoint[i];
            temp.to=toPoint[i];
            new LineAnimation().execute(temp);
            //

            T.startAnimation(animation);
            relativeLayout.removeView(T);
        }
    }

    public void updateDescription(String desc){
        description=(EditText)findViewById(R.id.description);
        description.setText(desc);
    }

    public void onLandmarkDetected(){
        RelativeLayout.LayoutParams param=new RelativeLayout.LayoutParams(220, 200);
        param.addRule(RelativeLayout.CENTER_HORIZONTAL);
        param.addRule(RelativeLayout.CENTER_VERTICAL);
        landmarkDetected = new ImageButton(this);
        landmarkDetected.setBackgroundResource(R.drawable.landmark_detected_icon);
        landmarkDetected.setLayoutParams(param);
        landmarkDetected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button_state==0){
                    addInfoButton();
                    button_state=1;
                }
                else {
                    button_state = 0;
                    deleteInfoButton();
                }
            }
        });
        landmarkDetected.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(button_state==1) return false;
                updateDescription("Long Clicked");
                return true;
            }
        });
        relativeLayout.addView(landmarkDetected);
    }

    private void loadImageFromStorage(String path)
    {

        imageView = (ImageView) findViewById(R.id.imageView);
        try {
            File f=new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            picture = b;
            imageView.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    public void foo(){
       // Toast.makeText(getApplicationContext(),"Hi",Toast.LENGTH_LONG).show();
        FirebaseVisionCloudDetectorOptions options =
                new FirebaseVisionCloudDetectorOptions.Builder()
                        .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                        .setMaxResults(15)
                        .build();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(picture);
        FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                .getVisionCloudLandmarkDetector();
        Task<List<FirebaseVisionCloudLandmark>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                        // Task completed successfully
                        // [START_EXCLUDE]
                        // [START get_landmarks_cloud]
                        Toast.makeText(getApplicationContext(),"entered suc",Toast.LENGTH_LONG).show();

                        for (FirebaseVisionCloudLandmark landmark: firebaseVisionCloudLandmarks) {

                            Rect bounds = landmark.getBoundingBox();
                            String landmarkName = landmark.getLandmark();
                            String entityId = landmark.getEntityId();
                            float confidence = landmark.getConfidence();
                            Toast.makeText(getApplicationContext(),landmarkName,Toast.LENGTH_LONG).show();
                            //Log.d("checking","landmark"+landmarkName);
                        }
                        // [END get_landmarks_cloud]
                        // [END_EXCLUDE]
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...

                        Toast.makeText(getApplicationContext(),"FAIL",Toast.LENGTH_LONG).show();

                    }
                });

    }

}
