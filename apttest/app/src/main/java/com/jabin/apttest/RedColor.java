package com.jabin.apttest;

import android.util.Log;

import com.jabin.apt.annotation.Factory;

@Factory(id = "RedColor", type = Color.class)
public class RedColor implements Color {

    @Override
    public void display() {
        Log.e(TAG, "display: red");
    }
}
