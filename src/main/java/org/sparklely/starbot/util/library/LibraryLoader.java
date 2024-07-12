package org.sparklely.starbot.util.library;

import net.byteflux.libby.Library;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LibraryLoader {
    public static LibraryManager manager = new LibraryManager();
    public static Map<String,Boolean> isLoad = new ConcurrentHashMap<>();

    public static void init(){
        manager.addRepository("https://maven.aliyun.com/repository/public"); // 使用阿里云的仓库
        //manager.addMavenCentral();
        LibraryLoader.load("org.jetbrains.kotlin","kotlin-stdlib","2.0.0","kotlin.Unit"); // Kotlin
        LibraryLoader.load("com.google.code.gson","gson","2.11.0","com.google.gson.Gson"); // Gson
    }
    /**
     * 加载依赖
     * @param groupId 依赖的组名
     * @param artifactId 依赖的名字
     * @param version 依赖的版本
     * @param tryClass 尝试加载的类,用于判断是否已经加载过
     */
    public static void load(String groupId,String artifactId,String version,String tryClass){
        try{
            Class.forName(tryClass);
        }catch (ClassNotFoundException e){
            // 没有这个类，加载
            load(groupId,artifactId,version);
        }
    }
    /**
     * 加载依赖
     * @param groupId 依赖的组名
     * @param artifactId 依赖的名字
     * @param version 依赖的版本
     */
    public static void load(String groupId,String artifactId,String version){
        String longName = groupId + artifactId;
        //如果没有加载过，则进行加载
        if(!isLoad.containsKey(longName) || !isLoad.get(longName)){
            loadLib(groupId,artifactId,version);
            isLoad.put(longName,true);
        }
    }

    private static void loadLib(String groupId, String artifactId, String version) {
        if (manager == null) {
            throw new IllegalStateException("LibraryManager is not initialized");
        }
        final Library library = Library.builder()
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .build();

        manager.loadLibrary(library);
    }


}
