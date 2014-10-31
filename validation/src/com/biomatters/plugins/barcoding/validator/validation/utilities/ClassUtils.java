package com.biomatters.plugins.barcoding.validator.validation.utilities;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * TODO: Write some javadoc
 *
 * @author Frank Lee
 *         Created on 30/10/14 5:35 PM
 */

public class ClassUtils {

    public static List<Class> findClass(String packageName, Class[] superClass) {
        List<Class> classes = null;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);
            List<File> dirs = new ArrayList<File>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                dirs.add(new File(resource.getFile()));
            }
            classes = new ArrayList<Class>();
            for (File directory : dirs) {
                classes.addAll(findClasses(directory, packageName, superClass));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }


    private static List<Class> findClasses(File directory, String packageName, Class[] superClass) throws
            ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName(), superClass));
            } else if (file.getName().endsWith(".class")) {
                Class cl = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));

                //do not return abstract class
                if (Modifier.isAbstract(cl.getModifiers())) {
                    continue;
                }

                int i;
                for (i = 0; i < superClass.length; i++) {
                    if (!superClass[i].isAssignableFrom(cl)) {
                        break;
                    }
                }

                if (i == superClass.length) {
                    classes.add(cl);
                }
            }
        }
        return classes;
    }
}