package org.sparklely.starbot;

import com.google.gson.Gson;
import org.sparklely.starbot.util.LibraryLoader;

public class Main {
    public static void main(String[] args) {
        // 初始化依赖加载器&加载必要的依赖
        System.out.println("开始加载依赖,请耐心等待哦");
        LibraryLoader.init();
        System.out.println("依赖加载完成!");
        new Gson();
    }
}