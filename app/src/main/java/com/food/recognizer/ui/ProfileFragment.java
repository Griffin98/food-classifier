package com.food.recognizer.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.food.recognizer.MyDBHelper;
import com.food.recognizer.R;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;

    private MyDBHelper db;

    private CircleImageView image;

    private String uid;

    private TextView name;
    private TextView age;
    private TextView gender;
    private TextView height;
    private TextView weight;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = new MyDBHelper(getContext());

        uid = mAuth.getCurrentUser().getUid();

        image = view.findViewById(R.id.profile_image);
        name = view.findViewById(R.id.profile_name);
        age = view.findViewById(R.id.profile_age);
        gender = view.findViewById(R.id.profile_gender);
        height = view.findViewById(R.id.profile_height);
        weight = view.findViewById(R.id.profile_weight);

        setUpProfilePage();
    }

    private void setUpProfilePage() {
        String uname = "Hello " + db.getUserName(uid) + " !";
        String ugender = db.getUserGender(uid);
        int uage = db.getUserAge(uid);
        int uheight = db.getUserHeight(uid);
        int uweight = db.getUserWeight(uid);
        Bitmap bmp = BitmapFactory.decodeByteArray(db.getUserImage(uid), 0, db.getUserImage(uid).length);

        name.setText(uname);
        if(uage != -1) {
            age.setText("" + uage);
        }
        if(ugender != null) {
            gender.setText(ugender);
        }
        if(uheight != -1) {
            height.setText(""+uheight);
        }
        if(uweight != -1) {
            weight.setText("" + uweight);
        }
        if(bmp != null){
            image.setImageBitmap(bmp);
        }


    }
}
