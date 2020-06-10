package com.jabin.apttest;

import android.util.Log;

import com.jabin.apt.annotation.Factory;

@Factory(id="Blue", type = Color.class)
public class Blue implements Color {

    @Override
    public void display() {
        Log.e(TAG, "display: blue" );
    }
}
