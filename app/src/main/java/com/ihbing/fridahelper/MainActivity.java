package com.ihbing.fridahelper;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.igio90.fridainjector.FridaAgent;
import com.igio90.fridainjector.FridaInjector;
import com.igio90.fridainjector.FridaMessage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

public class MainActivity extends Activity{
    public TextView fridaPrint;
    private FridaInjector fridaInjector;
    private FridaAgent fridaAgent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        EventBus.getDefault().register(this);
        fridaPrint=findViewById(R.id.frida_print);
        fridaPrint.setMovementMethod(ScrollingMovementMethod.getInstance());
        fridaPrint.setText("------------frida-----------------\n");
        fridaInit();
    }
    private void fridaInit(){
        try {
            // build an instance of FridaInjector providing binaries for arm/arm64/x86/x86_64 as needed
            // assets/frida-inject-12.8.2-android-arm64
            fridaInjector = new FridaInjector.Builder(this)
                    .withArm64Injector("fi12116")
                    .build();

            // build an instance of FridaAgent
            fridaAgent = new FridaAgent.Builder(this)
                    .withAgentFromAssets("agent.js")
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void fridaRun(View view) {
        // inject systemUi
        fridaInjector.inject(fridaAgent, "com.sec.android.easyMover", false);
//        fridaInjector.inject(fridaAgent, "com.android.systemui", true);
    }

    public void fridaStop(View view) {
        fridaInjector.unInject();
    }

    @Subscribe(threadMode=ThreadMode.MAIN)
    public void onMessage(FridaMessage msg) {
        fridaPrint.append(String.format("%s\n",msg.data ));
    }
}
