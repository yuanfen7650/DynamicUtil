package com.dynamicutils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.Window;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * 代理activity,继承自AppCompatActivity,则需要使用Theme.AppCompat下的样式，需使用v7兼容包
 * 需要在manifest中注册该activity(注意，请不要将此activity设成单利，如果设成单利那么插件内的跳转将无法执行)
 * 启动插件activity方法:
 * Intent intent=new Intent(this,DynamicChildActivity.class);
 * intent.putExtra("DynamicApkPath","/data/data/com.hld.testproject/files/a.apk");
 * intent.putExtra("DynamicActivityClassName","testproject.hld.com.testproject2.TestActivity");
 * startActivity(intent);
 */
public class DynamicChildActivity extends Activity{
    DynamicParantActivity dynamicParantActivity;

    String apkPath="";

    //加载dex
    public Class<?> getDynamicClass(String apkPath,String className){
        String apkFileDir = apkPath;
        String optDir = getDir("optdex", Context.MODE_PRIVATE).getAbsolutePath();
        new File(optDir).mkdirs();

        DexClassLoader classLoader = new DexClassLoader(apkFileDir, optDir, null,getClassLoader());
        try {
            Class<?> testClass = classLoader.loadClass(className+"");
            return testClass;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    Resources mResources=null;
    Resources.Theme mTheme;
    private AssetManager assetManager;

    public Resources getmResources() {
        if(mResources!=null){
            return mResources;
        }
        return super.getResources();
    }

    @Override
    public Resources.Theme getTheme() {
        if(mTheme!=null){
            return mTheme;
        }
        return super.getTheme();
    }

    @Override
    public AssetManager getAssets() {
        if(assetManager!=null){
            return assetManager;
        }
        return super.getAssets();
    }

    void initResources(String apkPath){
        try {
            Resources res=getResources();
            AssetManager assetManager=res.getAssets();

            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, apkPath);
            res.updateConfiguration(null,null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    protected Resources loadResources(String apkPath) {
        if(mResources==null){
            try {
                assetManager = AssetManager.class.newInstance();
                Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
                addAssetPath.invoke(assetManager, apkPath);

                Resources superRes = super.getResources();
                mResources = new Resources(assetManager, superRes.getDisplayMetrics(),superRes.getConfiguration());

                Resources.Theme superTheme=getBaseContext().getTheme();

                mTheme = mResources.newTheme();
                mTheme.setTo(superTheme);

//                mTheme.applyStyle(R.style.AppTheme, true);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mResources;
    }
    private void onDynamicCreate(Bundle savedInstanceState){
        apkPath=getIntent().getStringExtra("DynamicApkPath");//apk的位置
        String className=getIntent().getStringExtra("DynamicActivityClassName");//需要加载activity类名,getClassName

        String testClassName=getIntent().getStringExtra("testClassName");//测试加载类

        if(apkPath!=null&&!"".equals(apkPath)&&!"null".equals(apkPath)&&!"NULL".equals(apkPath)){
            try {
                Class cls=getDynamicClass(apkPath,className);
                dynamicParantActivity= (DynamicParantActivity) cls.newInstance();
                dynamicParantActivity.setApkPath(apkPath);
                dynamicParantActivity.setDynamicActivity(this);

                loadResources(apkPath);
                initResources(apkPath);

            }  catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }else if(testClassName!=null&&!"".equals(testClassName)){
            Class<?>cls= null;
            try {
                cls = Class.forName(testClassName);
                dynamicParantActivity= (DynamicParantActivity) cls.newInstance();
                dynamicParantActivity.setDynamicActivity(this);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        onDynamicCreate(savedInstanceState);

        super.onCreate(savedInstanceState);

        dynamicParantActivity.onCreate(savedInstanceState);

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {

        onDynamicCreate(savedInstanceState);

        super.onCreate(savedInstanceState, persistentState);

        dynamicParantActivity.onCreate(savedInstanceState);

    }

    @Override
    public void onPause() {
        super.onPause();
        dynamicParantActivity.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        dynamicParantActivity.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        dynamicParantActivity.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        dynamicParantActivity.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dynamicParantActivity.onDestroy();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        dynamicParantActivity.onRestart();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dynamicParantActivity.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean flag=dynamicParantActivity.onKeyUp(keyCode,event);
        if(flag){
            super.onKeyUp(keyCode, event);
        }
        return flag;
    }


    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        String tem=apkPath;
        if(tem!=null&&!"".equals(tem)){
            tem=tem.replaceAll("/","").replaceAll(".","");
        }
        return super.getSharedPreferences(tem+"_"+name, mode);
    }
}
