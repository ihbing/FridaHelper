package com.igio90.fridainjector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

public class FridaAgent {

    private static final String sWrapper = "" +"\n"+
            "console.log = function() {" +"\n"+
            "    var args = arguments;" +"\n"+
            "    Java.performNow(function() {" +"\n"+
            "        for (var i=0;i<args.length;i++) {" +"\n"+
            "            Java.use('android.util.Log').e('FridaAndroidInject', args[i].toString());" +"\n"+
            "        }" +"\n"+
            "    });" +"\n"+
            "};" +"\n"+
            "" +"\n"+
            "var send = function(data) {" +"\n"+
            "    Java.performNow(function () {" +"\n"+
            "        console.log('[call send]+'+data);" +"\n"+
            "        var Intent = Java.use('android.content.Intent');" +"\n"+
            "        var ComponentName = Java.use('android.content.ComponentName');" +"\n"+
            "        var ActivityThread = Java.use('android.app.ActivityThread');" +"\n"+
            "        var Context = Java.use('android.content.Context');" +"\n"+
            "        var ctx = Java.cast(ActivityThread.currentApplication().getApplicationContext(), Context);" +"\n"+
            "        var intent = Intent.$new();" +"\n"+
            "        intent.putExtra('data', JSON.stringify(data));" +"\n"+
            "        intent.setAction('com.frida.injector.SEND');" +"\n"+
            "        intent.addFlags(0x01000000);" +"\n"+
            "        intent.setComponent(ComponentName.$new('com.ihbing.fridahelper','com.igio90.fridainjector.DataBroadcast'));" +"\n"+
            "        ctx.sendBroadcast(intent);" +"\n"+
            "    });" +"\n"+
            "}" +"\n"+
            "\n";

    static final String sRegisterClassLoaderAgent = "" +
            "Java.performNow(function() {" +"\n"+
            "    var app = Java.use('android.app.ActivityThread').currentApplication();" +"\n"+
            "    var context = app.getApplicationContext();" +"\n"+
            "    var pm = context.getPackageManager();" +"\n"+
            "    var ai = pm.getApplicationInfo(context.getPackageName(), 0);" +"\n"+
            "    var apkPath = ai.publicSourceDir.value;" +"\n"+
            "    apkPath = apkPath.substring(0, apkPath.lastIndexOf('/')) + '/xd.apk';" +"\n"+
            "    var cl = Java.use('dalvik.system.DexClassLoader').$new(" +"\n"+
            "            apkPath, context.getCacheDir().getAbsolutePath(), null," +"\n"+
            "            context.getClass().getClassLoader());" +"\n"+
            "    Java.classFactory['xd_loader'] = cl;" +"\n"+
            "});" +"\n"+
            "\n";

    private final Context mContext;
    private final String mWrappedAgent;
    private final LinkedHashMap<String, Class<? extends FridaInterface>> mInterfaces =
            new LinkedHashMap<>();

    private FridaAgent(Builder builder) {
        mContext = builder.getContext();
        mWrappedAgent = builder.getWrappedAgent();
    }


    String getWrappedAgent() {
        return mWrappedAgent;
    }

    LinkedHashMap<String, Class<? extends FridaInterface>> getInterfaces() {
        return mInterfaces;
    }


    PackageManager getPackageManager() {
        return mContext.getPackageManager();
    }

    String getPackageName() {
        return mContext.getPackageName();
    }

    File getFilesDir() {
        return mContext.getFilesDir();
    }

    public void registerInterface(String cmd, Class<? extends FridaInterface> fridaInterface) {
        mInterfaces.put(cmd, fridaInterface);
    }

    public static class Builder {
        private final Context mContext;

        private String mWrappedAgent;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder withAgentFromAssets(String agentPath) throws IOException {
            String agent = Utils.readFromFile(mContext.getAssets().open(agentPath));
            return withAgentFromString(agent);
        }

        public Builder withAgentFromString(String agent) {
            mWrappedAgent = agent;
            return this;
        }


        public FridaAgent build() {
            if (mWrappedAgent == null) {
                throw new RuntimeException("no agent specified");
            }
            return new FridaAgent(this);
        }

        String getWrappedAgent() {
            return mWrappedAgent;
        }

        Context getContext() {
            return mContext;
        }
    }
}
