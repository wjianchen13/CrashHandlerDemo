package com.example.crashhandlerdemo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {

    public static final String TAG = MyExceptionHandler.class.getSimpleName();

    private Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;

    private static MyExceptionHandler INSTANCE;

    private Context mContext;

    private Map<String, String> deviceInfoMap = new HashMap<>();

    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss  ");

    private MyExceptionHandler() {
    }

    public static MyExceptionHandler getInstance() {
        if (INSTANCE == null) {
            synchronized(MyExceptionHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MyExceptionHandler();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultUncaughtExceptionHandler != null) {
            mDefaultUncaughtExceptionHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * 自己定义错误处理,收集错误信息 发送错误报告等操作均在此完毕.
     *
     * @param ex
     * @return true:假设处理了该异常信息;否则返回false.
     */
    private boolean handleException(final Throwable ex) {
        if (ex == null) {
            return false;
        }
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                ex.printStackTrace();
                Toast.makeText(mContext, "非常抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();
        collectDeviceInfo(mContext);
        saveCrashInfo2File(ex);
        return true;
    }

    /**
     * 收集设备參数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                deviceInfoMap.put("versionName", versionName);
                deviceInfoMap.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                deviceInfoMap.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 保存错误信息到文件里
     *
     * @param ex
     * @return 返回文件名称称,便于将文件传送到server
     */
    private String saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : deviceInfoMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        String time = formatter.format(new Date());
        sb.append(time + result);
        try {
            String fileName = formatter.format(new Date()) + "exception.log";
            File externalFile = mContext.getExternalFilesDir(null);
            if (externalFile != null && !TextUtils.isEmpty(externalFile.getAbsolutePath())) {
                String path = externalFile.getAbsolutePath() + "/exception/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName, true);
                fos.write((sb.toString()).getBytes());
                fos.close();
            } else {
                Log.e(TAG, "file path is null");
            }
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
        return null;
    }
}
