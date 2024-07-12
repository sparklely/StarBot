package org.sparklely.starbot.util;

import net.byteflux.libby.LibraryManager;
import net.byteflux.libby.classloader.URLClassLoaderHelper;
import net.byteflux.libby.logging.adapters.JDKLogAdapter;


import org.sparklely.starbot.Meta;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.logging.Logger;

public class LibraryLoader {
    public static LibraryManager manger = new Manger();
    public void init(){
        // 先别管
        LibrariesLoader.setManager(new Manger());
    }


    public static class Manger extends LibraryManager {
        private static String DATA_FOLDER = "cache";
        private static String DIR = "lib";

        private final URLClassLoaderHelper classLoader;
        public Manger() {
            super(new JDKLogAdapter(Logger.getLogger("LibraryLoader")),Path.of(DATA_FOLDER),DIR );
            this.classLoader = new URLClassLoaderHelper((URLClassLoader) Meta.INSTANCE.getClass().getClassLoader(), this);
        }

        @Override
        protected void addToClasspath(Path file) {
            this.classLoader.addToClasspath(file);
        }
    }
}
