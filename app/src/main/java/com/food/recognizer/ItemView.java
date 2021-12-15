package com.food.recognizer;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemView extends AppCompatActivity {

    private static final String TAG = "Food Recognizer :: ";

    private DatePickerDialog datePickerDialog;

    private Classifier classifier;

    private FirebaseAuth mAuth;

    private MyDBHelper db;

    private static final int INPUT_SIZE = 299;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    private static final String INPUT_NAME = "Mul:0";
    private static final String OUTPUT_NAME = "final_result";

    private static final String MODEL_FILE = "file:///android_asset/graph_new.pb";
    private static final String LABEL_FILE = "file:///android_asset/labels_new.txt";

    private String imagePath = null;

    private Button addButton;

    private TextView foodName;
    private TextView foodCalorie;
    private TextView foodCarbs;
    private TextView foodProtein;
    private TextView recordDate;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_item_view);

        mAuth = FirebaseAuth.getInstance();
        db = new MyDBHelper(getApplicationContext());

        ImageView imageView = findViewById(R.id.food_image);
        foodName = findViewById(R.id.text_food_name);
        foodCalorie = findViewById(R.id.text_food_cal);
        foodCarbs = findViewById(R.id.text_food_carbs);
        foodProtein = findViewById(R.id.text_food_proteins);

        recordDate = findViewById(R.id.text_record_date);
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        recordDate.setText(date);
        recordDate.setInputType(InputType.TYPE_NULL);
        recordDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                datePickerDialog = new DatePickerDialog(ItemView.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String selectedDate = sdf.format(new Date(year-1900, month,dayOfMonth));
                        recordDate.setText(selectedDate);
                    }
                }, year, month, day);
                datePickerDialog.show();

            }
        });


        addButton = findViewById(R.id.add_record_btn);
        addButton.setOnClickListener(addRecord);

        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            imagePath = getIntent().getExtras().get("ImagePath").toString();
            final Bitmap thumbnail = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(thumbnail);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    predict(imagePath);
                }
            }, 2000);
        }

    }

    View.OnClickListener addRecord = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(!foodName.getText().toString().isEmpty() && !foodCalorie.getText().toString().isEmpty()
            && !foodCarbs.getText().toString().isEmpty() && !foodProtein.getText().toString().isEmpty()) {

                String uid = mAuth.getCurrentUser().getUid();

                String foodname = foodName.getText().toString();
                String date = recordDate.getText().toString();
                byte[] foodimage = getBitmapAsByteArray(imagePath);
                int foodcalorie = Integer.parseInt(foodCalorie.getText().toString());
                int foodcarb = Integer.parseInt(foodCarbs.getText().toString());
                int foodprotein = Integer.parseInt(foodProtein.getText().toString());
                db.addMealRecord(uid, date, foodname, foodimage, foodcalorie, foodcarb, foodprotein);
                Toast.makeText(getApplicationContext(),"Record Added Successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Please wait recognizing food...", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public static byte[] getBitmapAsByteArray(String imgPath) {
        Bitmap thumbnail = BitmapFactory.decodeFile(imgPath);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(thumbnail, 299, 299, false);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }

    @SuppressLint("SetTextI18n")
    private void predict(String imagePath) {
        byte[] BYTE = getBitmapAsByteArray(imagePath);
        Bitmap inputBitmap = BitmapFactory.decodeByteArray(BYTE, 0, BYTE.length);

        classifier = TensorFlowImageClassifier.create(
                getAssets(),
                MODEL_FILE,
                LABEL_FILE,
                INPUT_SIZE,
                IMAGE_MEAN,
                IMAGE_STD,
                INPUT_NAME,
                OUTPUT_NAME);

        final List<Classifier.Recognition> results = classifier.recognizeImage(inputBitmap);

        List<String> categories = new ArrayList<String>();

        for(Classifier.Recognition result:results){
            Log.d(TAG, result.getTitle());
            categories.add(result.getTitle());
        }
        if(categories.size() ==0){
            foodName.setText("No record found");
            foodName.setError("No record found");
        } else {
            foodName.setText(categories.get(0).toUpperCase());

            for(int i=0; i < FoodNutritionDB.food_name.length; i++) {

                if(FoodNutritionDB.food_name[i].equals(categories.get(0))) {
                    foodCalorie.setText("" + FoodNutritionDB.food_calories[i]);
                    foodCarbs.setText("" + FoodNutritionDB.food_carbs[i]);
                    foodProtein.setText("" + FoodNutritionDB.food_proteins[i]);
                    break;
                }
            }
        }

    }

}
