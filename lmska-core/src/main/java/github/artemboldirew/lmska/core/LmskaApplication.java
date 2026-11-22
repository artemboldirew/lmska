package github.artemboldirew.lmska.core;

import com.github.artemboldirew.lmska.bring.ApplicationContext;
import com.github.artemboldirew.lmska.bring.BringApplication;
import com.github.artemboldirew.lmska.bring.annotation.ComponentScan;

/**
 * Запускает приложение через Bring
 */
@ComponentScan("github.artemboldirew.lmska")
public class LmskaApplication {
    public static void main(String[] args) {
        BringApplication.run(LmskaApplication.class);
    }
}
