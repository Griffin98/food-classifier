package com.food.recognizer.ui;

import android.graphics.Color;
import android.icu.util.LocaleData;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.food.recognizer.MyDBHelper;
import com.food.recognizer.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class DailyProgressFragment extends Fragment {

    private static final  String TAG = "Food Recognizer :: " + DailyProgressFragment.class.getSimpleName();

    private FirebaseAuth mAuth;

    private MyDBHelper db;

    private LineChart mLineChart;
    private PieChart mPieChart;

    private String uid;
    private String curDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_daily_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = new MyDBHelper(getContext());

        uid = mAuth.getCurrentUser().getUid();
        curDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        mLineChart = view.findViewById(R.id.linechart);
        mPieChart = view.findViewById(R.id.pie_chart);

        setUpPieChart();
        setUpLineChart();
    }

    private void setUpPieChart() {
        int calorie = db.getDailyCalorie(uid, curDate);
        int carbs = db.getDailyCarbs(uid, curDate);
        int protein = db.getDailyProtein(uid, curDate);
        int fat = calorie - (carbs + protein);

        if (calorie != -1 && carbs != -1 && protein != -1) {

            ArrayList<PieEntry> yValue = new ArrayList<>();
            yValue.add(new PieEntry((float)carbs,"Carbs"));
            yValue.add(new PieEntry((float)protein, "Proteins"));
            yValue.add(new PieEntry((float)fat , "Fats"));

            PieDataSet dataSet = new PieDataSet(yValue, "Source of Calorie");
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setValueTextSize(16f);

            PieData pieData = new PieData(dataSet);
            pieData.setValueFormatter(new PercentFormatter(mPieChart));
            mPieChart.setData(pieData);
            mPieChart.setUsePercentValues(true);
            mPieChart.getDescription().setEnabled(false);
            mPieChart.setCenterText("Today's source of Calorie");
            mPieChart.setCenterTextSize(16f);
            mPieChart.setEntryLabelColor(Color.BLACK);
            mPieChart.spin( 500,0,-360f, Easing.EaseInOutQuad);
            mPieChart.animate();
        }

    }

    private void setUpLineChart() {
        String day1 = getCalculatedDate("yyyy-MM-dd", -6);
        String day2 = getCalculatedDate("yyyy-MM-dd", -5);
        String day3 = getCalculatedDate("yyyy-MM-dd", -4);
        String day4 = getCalculatedDate("yyyy-MM-dd", -3);
        String day5 = getCalculatedDate("yyyy-MM-dd", -2);
        String day6 = getCalculatedDate("yyyy-MM-dd", -1);

        ArrayList<Entry> data = new ArrayList<>();
        data.add(new Entry(0, getCalorieFromDate(day1)));
        data.add(new Entry(1, getCalorieFromDate(day2)));
        data.add(new Entry(2, getCalorieFromDate(day3)));
        data.add(new Entry(3, getCalorieFromDate(day4)));
        data.add(new Entry(4, getCalorieFromDate(day5)));
        data.add(new Entry(5, getCalorieFromDate(day6)));
        data.add(new Entry(6, getCalorieFromDate(curDate)));

        final String[] labels = new String[] {
                getLabelFormattedDate(day1),
                getLabelFormattedDate(day2),
                getLabelFormattedDate(day3),
                getLabelFormattedDate(day4),
                getLabelFormattedDate(day5),
                getLabelFormattedDate(day6),
                getLabelFormattedDate(curDate)
        };
        ValueFormatter valueFormatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return labels[(int) value];
            }
        };
        float limit = (float) db.getUserGoal(uid);
        LimitLine ll = new LimitLine(limit, "Daily Goal");
        ll.setLineWidth(2f);
        ll.enableDashedLine(10f, 10f, 0f);
        ll.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll.setTextSize(10f);


        XAxis xaxis = mLineChart.getXAxis();
        xaxis.setAvoidFirstLastClipping(true);
        xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xaxis.setValueFormatter(valueFormatter);
        xaxis.setDrawGridLines(false);
        xaxis.setGranularity(1f);

        YAxis rightY = mLineChart.getAxisRight();
        rightY.setEnabled(false);

        YAxis leftY = mLineChart.getAxisLeft();
        leftY.setDrawGridLines(false);
        leftY.addLimitLine(ll);

        LineDataSet lineDataSet = new LineDataSet(data, "Daily Calorie Progress");
        lineDataSet.setDrawFilled(true);
        lineDataSet.setColor(Color.DKGRAY);
        lineDataSet.setCircleColor(Color.DKGRAY);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);


        LineData lineData = new LineData(lineDataSet);
        mLineChart.setData(lineData);
        mLineChart.getDescription().setEnabled(false);
        mLineChart.invalidate();
    }

    public static String getCalculatedDate(String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return s.format(new Date(cal.getTimeInMillis()));
    }

    public static String getLabelFormattedDate(String date)  {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d = null;
        try {

            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM");
        String labelDate = sdf1.format(d);
        
        return labelDate;
    }

    public int getCalorieFromDate(String date) {
        int value = db.getDailyCalorie(uid, date);

        if(value == -1)
            value = 0;

        return value;
    }
}
