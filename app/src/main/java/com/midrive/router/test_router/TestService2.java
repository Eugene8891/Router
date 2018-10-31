package com.midrive.router.test_router;

import android.content.Context;
import android.util.Log;

import com.midrive.voice.router_annotation.annotation.Route;
import com.midrive.voice.router_api.template.Provider;

@Route(path = "/service/testService2")
public class TestService2 implements Provider {
    @Override
    public void init(Context context) {

    }

    public void service() {
        Log.i("test router","TestService2 service");
    }
}
