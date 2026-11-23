package com.github.artemboldirew.lmska.common;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.stream.Stream;


public final class ReflectionUtils {
    private static final ClassFileParser PARSER = new ClassFileParser();

    private ReflectionUtils() {
    }

    public static Set<Class<?>> getAllInterfaces(Class<?> clazz) {
        Set<Class<?>> result = new HashSet<>();

        while (clazz != null) {
            for (Class<?> iface : clazz.getInterfaces()) {
                collectInterfaces(iface, result);
            }

            clazz = clazz.getSuperclass();
        }

        return result;
    }

    private static void collectInterfaces(Class<?> iface, Set<Class<?>> result) {
        if (!result.add(iface)) {
            return;
        }

        for (Class<?> parent : iface.getInterfaces()) {
            collectInterfaces(parent, result);
        }
    }

    public static Set<ClassMetadata> scanClassFiles(String basePackage) {
        if (basePackage == null || basePackage.isBlank()) {
            throw new IllegalArgumentException("Base package is empty");
        }

        Set<ClassMetadata> result = new LinkedHashSet<>();
        String packagePath = basePackage.replace('.', '/');

        try {
            Enumeration<URL> resources = getClassLoader().getResources(packagePath);

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();

                switch (url.getProtocol()) {
                    case "file" -> scanDirectory(Path.of(url.toURI()), result);
                    case "jar" -> scanJar(url, packagePath, result);
                    default -> throw new IOException("Unsupported protocol: " + url.getProtocol());
                }
            }

        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Failed to scan package: " + basePackage, e);
        }

        return result;
    }

    public static Set<Class<?>> loadClasses(Collection<ClassMetadata> metadata) {
        Set<Class<?>> result = new LinkedHashSet<>();

        for (ClassMetadata clazz : metadata) {
            result.add(loadClass(clazz.name()));
        }

        return result;
    }

    public static Set<Class<?>> findAnnotatedClasses(Collection<ClassMetadata> metadata, Class<? extends Annotation> annotation) {
        Set<Class<?>> result = new LinkedHashSet<>();
        String annotationName = annotation.getName();

        for (ClassMetadata clazz : metadata) {
            if (clazz.annotations().contains(annotationName)) {
                result.add(loadClass(clazz.name()));
            }
        }

        return result;
    }

    public static Class<?> loadClass(String className) {
        try {
            return Class.forName(className, false, getClassLoader());
        } catch (ClassNotFoundException | LinkageError e) {
            throw new IllegalStateException("Failed to load class: " + className, e);
        }
    }

    private static void scanDirectory(Path root, Set<ClassMetadata> result) throws IOException {

        try (Stream<Path> files = Files.walk(root)) {
            for (Path path : files.filter(Files::isRegularFile).filter(file -> isClassFile(file.toString())).toList()) {

                result.add(PARSER.parse(path));
            }
        }
    }

    private static void scanJar(URL url, String packagePath, Set<ClassMetadata> result) throws IOException {

        JarURLConnection connection = (JarURLConnection) url.openConnection();

        connection.setUseCaches(false);

        try (JarFile jar = connection.getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (!entry.isDirectory() && name.startsWith(packagePath + "/") && isClassFile(name)) {

                    try (InputStream in = jar.getInputStream(entry)) {
                        result.add(PARSER.parse(in));
                    }
                }
            }
        }
    }

    private static boolean isClassFile(String name) {
        return name.endsWith(".class") && !name.endsWith("package-info.class") && !name.endsWith("module-info.class");
    }

    private static ClassLoader getClassLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        return loader != null ? loader : ReflectionUtils.class.getClassLoader();
    }
}