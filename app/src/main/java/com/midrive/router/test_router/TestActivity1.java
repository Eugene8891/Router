package com.midrive.router.test_router;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.midrive.voice.router_annotation.annotation.Route;

@Route(path = "/activity/act1")
public class TestActivity1 extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("test router", "TestActivity1 onCreate");
    }
}
