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
import com.google.firebase.database.DatabaseReference;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

import com.google.firebase.database.FirebaseDatabase;
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

import java.util.ArrayList;
import java.util.Arrays;
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
    DatabaseReference mDatabase;


    static ArrayList<ArrayList<String>>arr=new ArrayList<ArrayList<String>>();

    ImageButton landmarkDetected;
    ImageButton []infoImageButtons;
    ImageButton []nearbyPlaceImageButton;

    Point []fromPoint;
    Point []toPoint;

    Point []nfromPoint;
    Point []ntoPoint;
    int []nbuttonX;
    int []nbuttonY;
    int idx;
    String []descriptions;
    AnimationSet as;
    String lat,lang;
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
        nfromPoint=new Point[3];
        ntoPoint=new Point[3];

        initPoints();
        ninitPoints();


        //number of info and nearby place buttons
        number_of_buttons=3;
        infoImageButtons=new ImageButton[number_of_buttons];
        nearbyPlaceImageButton=new ImageButton[number_of_buttons];
        buttonX=new int[number_of_buttons];
        buttonY=new int[number_of_buttons];
        nbuttonX=new int[number_of_buttons];
        nbuttonY=new int[number_of_buttons];
        descriptions=new String[number_of_buttons];
        initializeButtonXY();
        nInitializeButtonXY();

        initLocations();

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
        foo();

        updateDescription("LANDMARK DETECTED: ");
    }

    public void initPoints(){
        fromPoint[0]=new Point(560,470); toPoint[0]=new Point(790,320);
        fromPoint[1]=new Point(420,500); toPoint[1]=new Point(230,500);
        fromPoint[2]=new Point(560,530); toPoint[2]=new Point(790,650);
    }

    public void ninitPoints(){
        nfromPoint[0]=new Point(430,470); ntoPoint[0]=new Point(200,300);
        nfromPoint[1]=new Point(600,500); ntoPoint[1]=new Point(800,500);
        nfromPoint[2]=new Point(430,530); ntoPoint[2]=new Point(200,670);
    }

    public void initializeButtonXY(){
        buttonX[0]=50; buttonY[0]=800;
        buttonX[1]=750; buttonY[1]=400;
        buttonX[2]=750; buttonY[2]=1200;
    }

    public void nInitializeButtonXY(){
        nbuttonX[0]=50; nbuttonY[0]=400;
        nbuttonX[1]=50; nbuttonY[1]=1200;
        nbuttonX[2]=830; nbuttonY[2]=830;
    }
    void initLocations(){
        arr.add(new ArrayList<String>(Arrays.asList("Aguada Fort","15.4926","73.7732","Built by  Julião Simão in 1612","5 m high","Fort Aguada is a well-preserved seventeenth-century Portuguese fort, along with a lighthouse, standing in Goa, India, on Sinquerim Beach, overlooking the Arabian Sea.")));
        arr.add(new ArrayList<String>(Arrays.asList("Basilica of Bom Jesus","15.5009","73.9116","Built by  Julião Simão, Domingos Fernandes in 1605","18m high","The Basilica of Bom Jesus is a Roman Catholic basilica located in Goa, India, and is part of the Churches and convents of Goa UNESCO World Heritage Site. ")));
        arr.add(new ArrayList<String>(Arrays.asList("Shanta Durga Temple","15.3961","73.9856","constructed during the reign of Maratha Empire ruler Chattrapati Shahu Maharaj of Satara","25km2","Shri Shantadurga Temple is a Private temple complex belonging to Goud Saraswat Brahmin community. The temple is dedicated to Shantadurga, the goddess who mediates between Vishnu and Shiva. ")));
        arr.add(new ArrayList<String>(Arrays.asList("Mahadev temple","15.4390","74.2526","built by a minister of Yadava King Ramachandra","HEIGHT","Mahadeva Temple, is a 12th-century Shaivite temple dedicated to Lord Shiva and an active place of Hindu worship. It is the only structural temple of the Kadamba period to survive the destructive violence of religious intolerance during Muslim and Portuguese occupations of Goan territory.")));
        arr.add(new ArrayList<String>(Arrays.asList("Church of St. Francis of Assisi","15.5031","73.9112","established by eight Portuguese Franciscan friars","25km2","There are two large statues, one of St. Francis of Assisi and the other of the crucified Christ. The church’s gilded interior is decorated with paintings of events in the life of St. Francis, carved woodwork, additional statues, and frescos with intricate floral designs.")));
        arr.add(new ArrayList<String>(Arrays.asList("Dudhsagar Falls","15.3144","74.3143","BUILT BY..","320m","Dudhsagar Falls is amongst India's tallest waterfalls surrounded by deciduous forests with rich biodiversity")));
        arr.add(new ArrayList<String>(Arrays.asList("Ancestral Goa","15.3398","73.9864","maintained by Maendra Jocelino Araujo Alvares","30km2","Outdoor cultural museum featuring a recreated ancient Goa village, with pottery making & dancing")));
        arr.add(new ArrayList<String>(Arrays.asList("Anjuna Flea Market","15.5726","73.7424","Time-tested beachfront flea market open Wednesdays selling eclectic goods, including handmade items")));
        arr.add(new ArrayList<String>(Arrays.asList("St Cajetan Church","15.5056","73.9150","Built by 'Theatine Friars' ","21 feet","The church is in the form of a Greek cross and has a large dome with Latin inscriptions inside. The church has four granite statues of Saints")));
        arr.add(new ArrayList<String>(Arrays.asList("St. Augustine Tower","15.5005","73.9065","The church was built on top of the Monte Santo (Holy Hill), between 1597 and 1602 by Augustinian friars who landed in Goa in 1587","Height:46m","part of the World Heritage Site, Churches and convents of Goa.")));
        arr.add(new ArrayList<String>(Arrays.asList("Harvalem Caves","15.5527","74.0230","The caves were believed to be carved out of rock in the 6th century.","The caves were mined out of a single enormous laterite rock.", "Legend states that the five chambers of the caves were occupied by the five Pandava brothers, Yuddhistir, Bhim, Arjun, Nakul and Sahadev during their exile.")));
        arr.add(new ArrayList<String>(Arrays.asList("Indian Naval Aviation Museum", "15.3751", "73.8389", "The museum was established on 12th October 1998", "a", "b")));
        arr.add(new ArrayList<String>(Arrays.asList("Mae de Deus", "15.5471119", "73.7851869", "Dedicated to the Mother of God or Our Lady came to be built at Bardez in the 19th century", "14 kms away from Panaji", "It is a beautiful example of Gothic Architecture")));
        arr.add(new ArrayList<String>(Arrays.asList("Mahadev Temple", "15.43889", "74.252222", "Built in the Kadamba style from basalt", " 65 kilometres (40 mi) from the capital city of Panaji", "The temple is dedicated to Lord Shiva and is reminiscent of the temples at Aihole in neighbouring Karnataka")));
        arr.add(new ArrayList<String>(Arrays.asList("Mangueshi Temple", "15.4465956", "73.9678888", "This temple had its origins in Kushasthali Cortalim, a village in murmugao which fell to the invading Portuguese in 1543.", " 21 km from Panaji the capital of Goa", "The main temple is dedicated to Bhagavan Manguesh, an incarnation of Shiva. ")));
        arr.add(new ArrayList<String>(Arrays.asList("Menezes Braganza House", "15.261772", "74.043361", "Located in Chandor Village, Braganze House was built in the 17th century", "The house stretches along one while side of Chandor's village square, making it the biggest Portugese mansion of its kind in Goa.", "The West Wing houses the extensive Library of Dr. Luis de Menezes Bragancas, a noted journalist and leading light in the Goan Independence movement. The East Wing feautres a large ballroom with marble floor and a small family chapel, which contains acarefully hidden fingernail of St. Francis Xavier.")));
        arr.add(new ArrayList<String>(Arrays.asList("Immaculate Conception Church", "15.498916", "73.829362", "The colonial Portugese Baroque style church was first built in 1541 as a chapel. It was eventually replaced by a larger church in the 1600s", "The church is located in Panjim and sits atop a hill facing the square below. The walkway of zig-zag stairs ascending to the church were constructed out of laterite stone.", "The Festival of Our Lady of the Immaculate Conception is held every year on 8 December. The church is colourfully illuminated and a fair is also part of the festival.")));
        arr.add(new ArrayList<String>(Arrays.asList("Se Cathedral", "15.504068", "73.912229", "Construction of the cathedral was completed in 1619 and it was consecrated in 1640", "The church is 250 feet (76 m) in length and 181 feet (55 m) in breadth. The frontispiece stands 115 feet (35 m) high.", "The Se Cathedral's tower houses a large bell known as the 'Golden Bell' on account of its rich tone. It is said to be the largest in Goa, and one of the best in the world.")));
        arr.add(new ArrayList<String>(Arrays.asList("Goa State Museum", "15.493424", "73.833131", "The museum was established on 29 September 1977 in a rented building.", "The museum has fourteen galleries, arranged thematically. It has about 8,000 artifacts as well as 645 objects borrowed from the Institute Menezes Braganza Art Gallery and the Kala Academy.", "After building a new museum complex, the museum was formally inaugurated by the President of India.")));

    }

    ArrayList<String> retreiveLocationData(String place){
        ArrayList<String> temp=new ArrayList<String>();
        for(int i=0;i<arr.size();i++){
            if(arr.get(i).get(0).equals(place)) return arr.get(i);
        }
        return temp;
    }

    public void storeData(String place){

        ArrayList<String> temp=retreiveLocationData(place);
        lat=temp.get(1); lang=temp.get(2);
        descriptions[0]=temp.get(3);
        Log.d("desc0",descriptions[0]);
        descriptions[1]=temp.get(4);
        Log.d("desc1",descriptions[1]);
        descriptions[2]=temp.get(5);
        Log.d("desc2",descriptions[2]);
    }


    public void addInfoButton(){
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
            final String TEMP=descriptions[i];
            infoImageButtons[i]=new ImageButton(this);
            infoImageButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateDescription(TEMP);
                }
            });
            switch (i){
                case 0:
                    infoImageButtons[i].setBackgroundResource(R.drawable.built_by_icon); break;
                case 1:
                    infoImageButtons[i].setBackgroundResource(R.drawable.height_icon); break;
                case 2:
                    infoImageButtons[i].setBackgroundResource(R.drawable.general_description); break;

            }
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

//          // Line drawing section
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

    public void naddInfoButton(){
        Animation animation=AnimationUtils.loadAnimation(this,R.anim.fade_in);
        for(int i=0;i<number_of_buttons;i++){
            RelativeLayout.LayoutParams param=new RelativeLayout.LayoutParams(220, 200);
            param.leftMargin=nbuttonX[i];
            param.topMargin=nbuttonY[i];
            nearbyPlaceImageButton[i]=new ImageButton(this);
            final String l1=lat;
            final String l2=lang;
            nearbyPlaceImageButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =new Intent(DisplayPicture.this,Maps.class);
                    intent.putExtra("lat",l1);
                    intent.putExtra("long",l2);
                    startActivity(intent);
                }
            });
            switch (i){
                case 0:
                    nearbyPlaceImageButton[i].setBackgroundResource(R.drawable.waterfall_icon); break;
                case 1:
                    nearbyPlaceImageButton[i].setBackgroundResource(R.drawable.chruch_icon); break;
                case 2:
                    nearbyPlaceImageButton[i].setBackgroundResource(R.drawable.paw_icon); break;

            }
            nearbyPlaceImageButton[i].setLayoutParams(param);;

//            // Line drawing section
            TempClass temp=new TempClass();
            temp.canvas=mCanvas;
            temp.paint=p;
            temp.paint.setColor(Color.WHITE);
            temp.paint.setXfermode(null);
            temp.from=nfromPoint[i];
            temp.to=ntoPoint[i];
            new LineAnimation().execute(temp);
            //
            relativeLayout.addView(nearbyPlaceImageButton[i]);

            nearbyPlaceImageButton[i].startAnimation(animation);
        }
    }

    public void ndeleteInfoButton(){
        Animation animation=AnimationUtils.loadAnimation(this,R.anim.fade_out);
        for(int i=0;i<number_of_buttons;i++){
            ImageButton T = nearbyPlaceImageButton[i];

//            // Line drawing section
            TempClass temp=new TempClass();
            temp.canvas=mCanvas;
            temp.paint=p;
            temp.paint.setStrokeWidth(6);
            temp.paint.setColor(Color.TRANSPARENT);
            temp.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            temp.from=nfromPoint[i];
            temp.to=ntoPoint[i];
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
                else if(button_state==1){
                    button_state = 0;
                    deleteInfoButton();
                }
                else if(button_state==2){
                    button_state=0;
                    ndeleteInfoButton();
                }
            }
        });
        landmarkDetected.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(button_state==1) return false;
                button_state=2;
                naddInfoButton();
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
                        //Toast.makeText(getApplicationContext(),"entered suc",Toast.LENGTH_LONG).show();
                        int flag=0;
                        for (FirebaseVisionCloudLandmark landmark: firebaseVisionCloudLandmarks) {
                            flag=1;

                            Rect bounds = landmark.getBoundingBox();
                            String landmarkName = landmark.getLandmark();
                            String entityId = landmark.getEntityId();
                            float confidence = landmark.getConfidence();
                            updateDescription("LANDMARK DETECTED: "+ landmarkName);
                            mDatabase= FirebaseDatabase.getInstance().getReference();
                            mDatabase.child("temp").child(landmarkName).setValue(1);
                            storeData(landmarkName);

                            break;
                            //Toast.makeText(getApplicationContext(),landmarkName,Toast.LENGTH_LONG).show();
                            //Log.d("checking","landmark"+landmarkName);
                        }
                        if(flag==1){
                            onLandmarkDetected();
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
