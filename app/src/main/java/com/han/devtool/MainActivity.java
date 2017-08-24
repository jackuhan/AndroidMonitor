package com.han.devtool;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.facebook.react.devsupport.DebugOverlayController;
import com.han.devtool.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private DebugOverlayController mDebugOverlayController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void openFPS(){
        if (mDebugOverlayController == null) {
            mDebugOverlayController = new DebugOverlayController(this);
        }
        mDebugOverlayController.setFpsDebugViewVisible();
    }

    public void closeFPS(){
        if (mDebugOverlayController != null) {
            mDebugOverlayController.stopFps();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1: {
                openFPS();
                break;
            }
            case R.id.button2: {
                closeFPS();
                break;
            }


        }
    }

    @Override protected void onDestroy() {
        closeFPS();
        super.onDestroy();
    }
}
