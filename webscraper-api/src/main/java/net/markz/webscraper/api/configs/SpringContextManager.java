package net.markz.webscraper.api.configs;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.Arrays;

@Configuration
@ComponentScan(basePackages = "net.markz.webscraper.api")
@Slf4j
public class SpringContextManager {
    private static AnnotationConfigApplicationContext springContext;

    public static synchronized void startSpringContext() {
        if(springContext == null || !springContext.isActive()) {
            System.setProperty("spring.profiles.default", "prod");
            log.info(
                    "Initializing spring context: active={}, default={}",
                    System.getProperty("spring.profiles.active"),
                    System.getProperty("spring.profiles.default")
            );
            springContext = new AnnotationConfigApplicationContext(SpringContextManager.class);

            log.info(
                    "Initialized spring context: activeProfiles={}, defaultProfiles={}",
                    Arrays.toString(springContext.getEnvironment().getActiveProfiles()),
                    Arrays.toString(springContext.getEnvironment().getDefaultProfiles()));

        }
    }

    public static <T> T getBean(Class<T> beanClass) {
        return getContext().getBean(beanClass);
    }

    public static ApplicationContext getContext() {
        startSpringContext();
        return springContext;
    }

    @PreDestroy
    public static void destroyContext() {
        if(springContext != null) {
            springContext.close();
        }
    }

}
