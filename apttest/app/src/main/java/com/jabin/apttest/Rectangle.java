package com.jabin.apttest;

import android.util.Log;

import com.jabin.apt.annotation.Factory;

@Factory(id = "Rectangle", type = IShape.class)
public class Rectangle implements IShape {
    private static final String TAG = "Rectangle";

    @Override
    public void draw() {
        Log.e(TAG, "draw: ");
    }
}
