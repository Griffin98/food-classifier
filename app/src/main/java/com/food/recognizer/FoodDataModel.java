package com.food.recognizer;

public class FoodDataModel {

    private byte[] image;

    private int calorie;
    private int carbs;
    private int proteins;

    private String name;

    public FoodDataModel(String name, byte[] image, int calorie, int carbs, int proteins) {
        this.name = name;
        this.image = image;
        this.calorie = calorie;
        this.carbs = carbs;
        this.proteins = proteins;
    }


    public int getCalorie() {
        return calorie;
    }

    public void setCalorie(int calorie) {
        this.calorie = calorie;
    }

    public int getCarbs() {
        return carbs;
    }

    public void setCarbs(int carbs) {
        this.carbs = carbs;
    }

    public int getProteins() {
        return proteins;
    }

    public void setProteins(int proteins) {
        this.proteins = proteins;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
