package com.food.recognizer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.food.recognizer.LoginActivity;
import com.food.recognizer.ui.ProfileFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Food Recognizer :: " + MainActivity.class.getSimpleName();

    private AppBarConfiguration mAppBarConfiguration;

    private FirebaseAuth mAuth;

    private MyDBHelper db;

    private FrameLayout frameLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = new MyDBHelper(getApplicationContext());

        frameLayout = findViewById(R.id.frame_container);

        //signInChecker();

        if(mAuth.getCurrentUser() != null) {

            if(db.checkIfUserExists(mAuth.getCurrentUser().getUid())) {

                setContentView(R.layout.activity_main);
                Toolbar toolbar = findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                final DrawerLayout drawer = findViewById(R.id.drawer_layout);
                NavigationView navigationView = findViewById(R.id.nav_view);
                // Passing each menu ID as a set of Ids because each
                // menu should be considered as top level destinations.
                mAppBarConfiguration = new AppBarConfiguration.Builder(
                        R.id.nav_home, R.id.nav_profile, R.id.nav_progress, R.id.nav_history, R.id.nav_sign_out)
                        .setDrawerLayout(drawer)
                        .build();
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
                NavigationUI.setupWithNavController(navigationView, navController);

                View headerView = navigationView.getHeaderView(0);

                TextView name_header = headerView.findViewById(R.id.username_header);
                String name = db.getUserName(mAuth.getCurrentUser().getUid());
                if(name != null){
                    name_header.setText(name);
                }

                TextView email_header = (TextView) headerView.findViewById(R.id.email_header);
                email_header.setText(mAuth.getCurrentUser().getEmail());

                ImageView image_header = headerView.findViewById(R.id.imageView);
                byte[] imageByte = db.getUserImage(mAuth.getCurrentUser().getUid());
                Bitmap bmp = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                image_header.setImageBitmap(bmp);


            } else {
                Intent intent = new Intent(this, InfoActivity.class);
                startActivity(intent);
                finish();
            }

        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void signInChecker() {
        if(mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.d(TAG, "User Logged in" + mAuth.getCurrentUser().getUid());
        }

        if(!db.checkIfUserExists(mAuth.getCurrentUser().getUid())) {
            Intent intent = new Intent(this, InfoActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.d(TAG,"User data exists in db");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


    }


}
