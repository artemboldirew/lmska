package com.github.artemboldirew.lmska.bring;

/**
 * Точка запуска приложения с созданием контекста Bring
 */
public final class BringApplication {
    private BringApplication() {
    }

    /**
     * Создаёт контекст и вызывает ApplicationRunner
     * При ошибке закрывает контекст
     */
    public static ApplicationContext run(Class<?> configClass) {
        // 1) Созданием объект конфигурации
        Environment environment = new Environment();

        // 2) Сканируем файлы и создаем контекст
        ApplicationContext context = new ApplicationContext(environment);
        try {
            context.refresh(configClass);

            // 3) Если есть пользовательский код, который должен быть запущен после контекста, то запускаем
            ApplicationRunner runner = context.getBean(ApplicationRunner.class);

            runner.run();
            return context;
        } catch (RuntimeException | Error e) {
            try {
                context.close();
            } catch (RuntimeException closeError) {
                e.addSuppressed(closeError);
            }
            throw e;
        }
    }
}
