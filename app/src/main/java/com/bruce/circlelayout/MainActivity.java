package com.bruce.circlelayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bruce.circlelayout.widgets.CircleLayout;
import com.bruce.circlelayout.widgets.CircleView;
import com.orhanobut.logger.Logger;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private CircleLayout mCircleLayout;
    private ImageView mCenterSwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mCircleLayout = (CircleLayout) findViewById(R.id.circle_layout);
//        mCenterSwitch = (ImageView) findViewById(R.id.center_switch);
//        mCenterSwitch.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
//        mCircleLayout.showAndHide();
    }
}
