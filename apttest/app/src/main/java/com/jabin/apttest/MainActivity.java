package com.jabin.apttest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private IShapeFactory shapeFactory;
    private ColorFactory colorFactory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shapeFactory = new IShapeFactory();
        IShape shape = shapeFactory.create("Rectangle");
        shape.draw();
        IShape circle = shapeFactory.create("Circle");
        circle.draw();

        colorFactory = new ColorFactory();
        Color red = colorFactory.create("RedColor");
        red.display();
        Color blue = colorFactory.create("Blue");
        blue.display();
    }
}
