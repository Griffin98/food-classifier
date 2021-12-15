package com.food.recognizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Context context;

    private ArrayList<FoodDataModel> foodDataModels;

    public MyAdapter(Context context, ArrayList<FoodDataModel> foodDataModels) {
        this.context = context;
        this.foodDataModels = foodDataModels;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item_single, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final FoodDataModel food = foodDataModels.get(position);

        byte[] imgbyte = food.getImage();
        Bitmap bmp = BitmapFactory.decodeByteArray(imgbyte, 0, imgbyte.length);
        holder.imageView.setImageBitmap(bmp);
        holder.fname.setText(food.getName());
        holder.fcal.setText("" + food.getCalorie());
        holder.fcarb.setText("" + food.getCarbs());
        holder.fprot.setText("" + food.getProteins());
    }

    @Override
    public int getItemCount() {
        return foodDataModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imageView;
        TextView fname, fcal, fcarb, fprot;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.fimage);
            fname = itemView.findViewById(R.id.fname);
            fcal = itemView.findViewById(R.id.fcal);
            fcarb = itemView.findViewById(R.id.fcarbs);
            fprot = itemView.findViewById(R.id.fproteins);
        }
    }
}
