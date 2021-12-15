package com.food.recognizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class InfoActivity extends AppCompatActivity {

    private static final String TAG = "Food Recognizer :: " + InfoActivity.class.getSimpleName();

    private static final int PERMISSION_REQUEST_CODE = 200;

    private FirebaseAuth mAuth;

    private CircleImageView image;

    private EditText name;
    private EditText age;
    private EditText height;
    private EditText weight;
    private EditText goal;

    private MyDBHelper db;

    private RadioGroup radioGroup;
    private RadioButton male;
    private RadioButton female;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_basic_info);

        mAuth = FirebaseAuth.getInstance();
        db = new MyDBHelper(getApplicationContext());

        if( mAuth.getCurrentUser() == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        name = findViewById(R.id.name);
        image = findViewById(R.id.user_image);
        age = findViewById(R.id.age);
        height = findViewById(R.id.height);
        weight = findViewById(R.id.weight);
        goal = findViewById(R.id.goal);
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        radioGroup = findViewById(R.id.gender_group);
        radioGroup.clearCheck();

        name.addTextChangedListener(nameWatcher);
        age.addTextChangedListener(ageWatcher);
        height.addTextChangedListener(heightWatcher);
        weight.addTextChangedListener(weightWatcher);
        goal.addTextChangedListener(goalWatcher);


        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkPermission()) {
                    requestPermission();
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,1);
                }

            }
        });


        final ImageButton button = findViewById(R.id.next_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isValidName() && isValidAge() && isValidHeight() && isValidWeight()
                        && isValidGoal()) {

                    BitmapDrawable bitmapDrawable = (BitmapDrawable) image.getDrawable();
                    Bitmap bmp = bitmapDrawable.getBitmap();
                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, bs);

                    String uid = mAuth.getCurrentUser().getUid();
                    String uname = name.getText().toString();
                    byte[] uimage = bs.toByteArray();
                    int uage = Integer.parseInt(age.getText().toString());
                    int uheight = Integer.parseInt(height.getText().toString());
                    int uweight = Integer.parseInt(weight.getText().toString());
                    int ugoal = Integer.parseInt(goal.getText().toString());
                    String gender;

                    if(male.isSelected()) {
                        gender = (String) male.getText();

                    } else if (female.isSelected()) {
                        gender = (String) female.getText();

                    } else {
                        gender = "Male";
                    }

                    db.addUserRecord(uid, uname, uimage, uage, gender, uheight, uweight, ugoal);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();


                } else {
                    Log.d(TAG, "We are here");
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {

            if(requestCode == 1) {
                Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                final Bitmap bmp = BitmapFactory.decodeFile(picturePath);
                image.setImageBitmap(bmp);
            }

        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);

        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED &&
                result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE,CAMERA}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted){
                        // granted
                    }
                    else {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getApplicationContext())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private TextWatcher nameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            isValidName();
        }
    };

    private TextWatcher ageWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            isValidAge();
        }
    };

    private TextWatcher heightWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            isValidHeight();
        }
    };

    private TextWatcher weightWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            isValidWeight();
        }
    };

    private TextWatcher goalWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            isValidGoal();
        }
    };

    private boolean isValidName() {

        if(name.getText().toString().isEmpty()) {
            name.setError("Enter Name");
            return false;
        }

        return true;
    }

    private boolean isValidAge() {

        if(age.getText().toString().isEmpty()) {
            age.setError("Enter Age");
            return false;
        }

        if(Integer.parseInt(age.getText().toString()) < 0 ) {
            age.setError("Enter Valid Age");
            return false;
        }
        return true;
    }

    private boolean isValidHeight() {

        if(height.getText().toString().isEmpty()) {
            height.setError("Enter Height");
            return false;
        }

        if(Integer.parseInt(height.getText().toString()) < 20) {
            height.setError("Enter Valid Height");
            return false;
        }
        return true;
    }

    private boolean isValidWeight() {

        if(weight.getText().toString().isEmpty()) {
            weight.setError("Enter Weight");
            return false;
        }

        if(Integer.parseInt(weight.getText().toString()) < 10) {
            weight.setError("Enter Valid Height");
            return false;
        }
        return true;
    }

    private boolean isValidGoal() {

        if(goal.getText().toString().isEmpty()) {
            goal.setError("Enter goal");
            return false;
        }
        return true;
    }

}
