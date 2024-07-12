package org.sparklely.starbot.util.library;

import net.byteflux.libby.Library;
import net.byteflux.libby.logging.adapters.JDKLogAdapter;
import net.byteflux.libby.logging.adapters.LogAdapter;
import org.sparklely.starbot.Main;

import java.lang.module.ModuleFinder;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class LibraryManager extends net.byteflux.libby.LibraryManager{
    protected LibraryManager() {
        super(new JDKLogAdapter(Logger.getLogger("LibraryManager")), Path.of("cache"),"libs");
    }

    @Override
    protected void addToClasspath(Path file) {

    }

    @Override
    public void loadLibrary(Library library) {
        try {
            Path file = this.downloadLibrary((Library) Objects.requireNonNull(library, "library"));

            // 创建 URLClassLoader 对象
            URL jarUrl = new URL("file:" + file);
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl});

            // 打开 JAR 文件
            JarFile jarFile = new JarFile(file.toFile());

            // 遍历 JAR 文件中的所有条目
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                // 跳过 module-info.class 文件
                if (entry.getName().contains("module-info")) {
                    continue;
                }
                // 检查条目是否为类文件
                if (entry.getName().endsWith(".class")) {
                    // 获取类名
                    String className = entry.getName().replace("/", ".").substring(0, entry.getName().length() - 6);

                    // 加载类
                    try {
                        classLoader.loadClass(className);
                    } catch (Exception e) {
                        // 处理异常
                    }
                }
            }

            // 关闭 JAR 文件
            jarFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
