package github.artemboldirew.lmska.cli.utils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public final class ReflectionUtils {
    public Set<Class<?>> findAnnotatedClasses(String packageName, Class<? extends Annotation> annotation) {
        Set<Class<?>> result = new HashSet<>();
        String packagePath = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try {
            Enumeration<URL> resources = classLoader.getResources(packagePath);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();

                if ("file".equals(resource.getProtocol())) {
                    scanDirectory(packageName, new File(resource.toURI()), annotation, result);
                }
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to scan package: " + packageName, e);
        }
    }

    private void scanDirectory(
            String packageName,
            File directory,
            Class<? extends Annotation> annotation,
            Set<Class<?>> result
    ) throws ClassNotFoundException {

        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(packageName + "." + file.getName(), file, annotation, result);
                continue;
            }

            if (!file.getName().endsWith(".class")) {
                continue;
            }

            String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);

            Class<?> clazz =
                    Class.forName(className);

            if (clazz.isAnnotationPresent(annotation)) {
                result.add(clazz);
            }
        }
    }
}
