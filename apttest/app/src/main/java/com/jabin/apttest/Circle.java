package com.jabin.apttest;

import android.util.Log;

import com.jabin.apt.annotation.Factory;


@Factory(id = "Circle", type = IShape.class)
public class Circle implements IShape {
    private static final String TAG = "Circle";
    @Override
    public void draw() {
        Log.e(TAG, "draw: ");
    }
}
