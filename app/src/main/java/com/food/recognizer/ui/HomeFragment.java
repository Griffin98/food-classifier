package com.food.recognizer.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.food.recognizer.FoodDataModel;
import com.food.recognizer.ItemView;
import com.food.recognizer.MyAdapter;
import com.food.recognizer.MyDBHelper;
import com.food.recognizer.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    private static final String TAG = "Food Recognizer :: " + HomeFragment.class.getSimpleName();

    private static final int PERMISSION_REQUEST_CODE = 200;

    private ArrayList<String> foodName;
    private ArrayList<Integer> foodCalorie, foodCarbs, foodProteins;
    private ArrayList<byte[]> foodImage;

    private FirebaseAuth mAuth;

    private MyDBHelper db;
    private MyAdapter adapter;

    private FloatingActionButton mCameraButton;

    private ContentLoadingProgressBar progressBar;

    private RecyclerView recyclerView;

    private String uid;
    private String curDate;

    private TextView dummyTextView;
    private TextView nameTextView;
    private TextView goalTextView;
    private TextView goalCompletedTextView;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = new MyDBHelper(view.getContext());

        foodName = new ArrayList<>();
        foodCalorie = new ArrayList<>();
        foodCarbs = new ArrayList<>();
        foodProteins = new ArrayList<>();
        foodImage = new ArrayList<>();

        nameTextView = view.findViewById(R.id.name_home_card);
        goalTextView = view.findViewById(R.id.goal_home_card);
        goalCompletedTextView = view.findViewById(R.id.goal_completed_home_card);
        dummyTextView = view.findViewById(R.id.dummy_Text);

        progressBar = view.findViewById(R.id.progress_bar);

        recyclerView = view.findViewById(R.id.home_recycler);

        uid = mAuth.getCurrentUser().getUid();
        curDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        mCameraButton = view.findViewById(R.id.camera_btn);
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermission()) {
                    requestPermission();
                } else {
                    showImageSelectionDialog();
                }

            }
        });

    }

    private void showImageSelectionDialog() {
        final CharSequence[] options = {"Take Photo", "Choose Image from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Recognize Food !");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(options[which].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 1);
                } else if (options[which].equals("Choose Image from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(intent, 2);
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Intent intent = new Intent(getContext(), ItemView.class);
        if( resultCode == RESULT_OK ) {

            if (requestCode ==1) {

                Bitmap photo = (Bitmap) data.getExtras().get("data");

                File f = new File(Environment.getExternalStorageDirectory().toString());
                try {
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + String.valueOf(System.currentTimeMillis()) + ".jpg";
                    Log.d(TAG, "image path:" + path);
                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(path);
                    try {
                        outFile = new FileOutputStream(file);
                        photo.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();

                        intent.putExtra("ImagePath", path);
                        startActivity(intent);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if(requestCode == 2) {
                Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getActivity().getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                intent.putExtra("ImagePath", picturePath);
                startActivity(intent);
            }

        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getContext(), READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getContext(), CAMERA);

        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED &&
                result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE,CAMERA}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted)
                        Snackbar.make(getView(), "Permission Granted, Now you can access storage and camera.", Snackbar.LENGTH_LONG).show();
                    else {

                        Snackbar.make(getView(), "Permission Denied, You cannot access storage and camera.", Snackbar.LENGTH_LONG).show();

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
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMainCard();
        setUpMealCards();
    }

    private void setUpMainCard() {
        String uname =  "Hello " + db.getUserName(uid) + " !";
        if(uname != null) {
            nameTextView.setText(uname);
        }

        int ugoal = db.getUserGoal(uid);
        if (ugoal != -1){
            goalTextView.setText("" + ugoal);
        }

        int cgoal = db.getCompletedGoal(uid, curDate);
        if(cgoal != -1) {
            int progressPercent = (int) ( ( (float) cgoal / (float) ugoal ) * 100 );
            goalCompletedTextView.setText("" + cgoal);
            progressBar.setProgress(progressPercent);
        }


    }

    private void setUpMealCards() {
        ArrayList<FoodDataModel> mealList = new ArrayList<>();
        mealList = db.getMealRecord(uid, curDate);

        if( mealList.size() > 0 ) {
            dummyTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new MyAdapter(getContext(), mealList);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
        }

    }
}
