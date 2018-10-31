package com.midrive.voice.router_api.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import com.midrive.voice.router_api.thread.DefaultPoolExecutor;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

public class ClassUtils {
    /**
     * 通过指定包名，扫描包下面包含的所有的ClassName
     *
     * @param context     U know
     * @param packageName 包名
     * @return 所有class的集合
     */
    public static Set<String> getFileNameByPackageName(Context context, final String packageName) throws IOException, InterruptedException {
        final Set<String> classNames = new HashSet<>();


        DexFile df = new DexFile(context.getPackageCodePath());
        for (Enumeration<String> iter = df.entries(); iter.hasMoreElements();) {
            String className = iter.nextElement();
            if (className.contains(packageName)) {
                classNames.add(className);
            }
        }

        /*
        PathClassLoader pcl = new PathClassLoader(context.getPackageCodePath(), ClassLoader.getSystemClassLoader());
        Enumeration<URL> resources = pcl.getResources(packageName.replace('.','/'));

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String className = resource.getFile();
            if (className.contains(packageName)) {
                Log.i("tag",className);
            }
        }
        */
        if(classNames.size() == 0) {
            throw new RuntimeException("inited failed because do not generate classes");
        }
        return classNames;
    }

}
