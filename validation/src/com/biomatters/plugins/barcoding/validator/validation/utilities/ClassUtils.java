package com.biomatters.plugins.barcoding.validator.validation.utilities;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Util class used to find descendant class of specific superclass
 *
 * @author Frank Lee
 *         Created on 30/10/14 5:35 PM
 */

public class ClassUtils {

    @SuppressWarnings("unchecked")
    public static List<Class> findClass(String packageName, Class[] superClass) {
        List<Class> classes = new ArrayList<Class>();
        if (packageName == null || packageName.trim().length() == 0 || superClass == null || superClass.length == 0) {
            return classes;
        }

        List<String> files = new ArrayList<String>();

        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        try {
            String path = packageName.replace('.', '/');
            ClassLoader classLoader = superClass[0].getClassLoader();
            if (classLoader != null) {
                Thread.currentThread().setContextClassLoader(classLoader);
            }

            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if ("jar".equals(resource.getProtocol())) {
                    files.addAll(findClassesFromJar(resource.getPath()));
                } else if ("file".equals(resource.getProtocol())) {
                    files.addAll(findClassesFromFile(new File(resource.getPath()), packageName));
                }
            }

            for (String file : files) {
                Class cl;
                try {
                    cl = Class.forName(file);
                } catch (Throwable e) {
                    continue;
                }

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
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }

        return classes;
    }

    private static List<String> findClassesFromJar(String jarPath) {
        List<String> myClassName = new ArrayList<String>();
        String[] jarInfo = jarPath.split("!");
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
        String packagePath = jarInfo[1].substring(1);
        try {
            JarFile jarFile = new JarFile(jarFilePath);
            Enumeration<JarEntry> entrys = jarFile.entries();
            while (entrys.hasMoreElements()) {
                JarEntry jarEntry = entrys.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.endsWith(".class") && entryName.startsWith(packagePath)) {
                    entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                    myClassName.add(entryName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return myClassName;
    }

    private static List<String> findClassesFromFile(File directory, String packageName) {
        List<String> classes = new ArrayList<String>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getName().contains(".")) {
                    continue;
                }
                classes.addAll(findClassesFromFile(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
            }
        }
        return classes;
    }
}