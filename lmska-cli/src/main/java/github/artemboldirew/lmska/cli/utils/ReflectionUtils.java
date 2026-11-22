package github.artemboldirew.lmska.cli.utils;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Ищет классы команд в папках и JAR-файлах
 */
public final class ReflectionUtils {
    public Set<Class<?>> findAnnotatedClasses(String packageName, Class<? extends Annotation> annotation) {
        var metadata = com.github.artemboldirew.lmska.common.ReflectionUtils.scanClassFiles(packageName);
        return com.github.artemboldirew.lmska.common.ReflectionUtils.findAnnotatedClasses(metadata, annotation);
    }
}
