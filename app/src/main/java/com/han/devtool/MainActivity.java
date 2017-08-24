package com.han.devtool;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.facebook.react.devsupport.DebugOverlayController;
import com.han.devtool.R;

public class MainActivity extends AppCompatActivity {
    private DebugOverlayController mDebugOverlayController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDebugOverlayController = new DebugOverlayController(this);

        if (mDebugOverlayController != null) {
            mDebugOverlayController.setFpsDebugViewVisible(true);
        }
    }
}
