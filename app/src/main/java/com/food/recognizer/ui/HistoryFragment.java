package com.food.recognizer.ui;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import com.food.recognizer.FoodDataModel;
import com.food.recognizer.MyAdapter;
import com.food.recognizer.MyDBHelper;
import com.food.recognizer.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class HistoryFragment extends Fragment {

    private static final String TAG = " Food Recognizer :: " + HistoryFragment.class.getSimpleName();

    private ArrayList<FoodDataModel> mealList = new ArrayList<>();

    private CalendarView mCalendarView;

    private FirebaseAuth mAuth;

    private MyAdapter adapter;
    private MyDBHelper db;

    private RecyclerView mRecyclerView;

    private String uid;
    private String curDate;

    private TextView dummyTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCalendarView = view.findViewById(R.id.calendar);
        mRecyclerView = view.findViewById(R.id.previous_record);
        dummyTextView = view.findViewById(R.id.dummy_Text_history);

        mAuth = FirebaseAuth.getInstance();
        db = new MyDBHelper(view.getContext());

        uid = mAuth.getCurrentUser().getUid();
        curDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        adapter = new MyAdapter(getContext(), mealList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(adapter);

        setUpInitialCard();

        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                mealList.clear();
                adapter.notifyDataSetChanged();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String selectedDate = sdf.format(new Date(year-1900,month,dayOfMonth));
                Log.d(TAG, "date:" + selectedDate);
                mealList = db.getMealRecord(uid, selectedDate);

                if( mealList.size() > 0 ) {
                    dummyTextView.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    adapter = new MyAdapter(getContext(), mealList);
                    mRecyclerView.setHasFixedSize(true);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    mRecyclerView.setAdapter(adapter);
                } else {
                    mRecyclerView.setVisibility(View.GONE);
                    dummyTextView.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    private void setUpInitialCard() {
        mealList = db.getMealRecord(uid, curDate);

        if( mealList.size() > 0 ) {
            dummyTextView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();

        } else {
            mRecyclerView.setVisibility(View.GONE);
            dummyTextView.setVisibility(View.VISIBLE);
        }
    }
}
