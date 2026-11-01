package com.github.artemboldirew.lmska.bring;

public final class BringApplication {
    private BringApplication() {
    }

    public static ApplicationContext run(Class<?> configClass) {
        ApplicationContext context = new ApplicationContext();
        context.refresh(configClass);
        return context;
    }
}