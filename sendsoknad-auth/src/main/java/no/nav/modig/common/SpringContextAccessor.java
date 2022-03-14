package no.nav.modig.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This class can be used were you need access to a Spring bean from non-Spring manged class or POJO.
 * See: https://confluence.jaytaala.com/display/TKB/Super+simple+approach+to+accessing+Spring+beans+from+non-Spring+managed+classes+and+POJOs
 */
public class SpringContextAccessor implements ApplicationContextAware {

    private static ApplicationContext context;

    /**
     * Returns the Spring managed bean instance of the given class type if it exists.
     * Returns null otherwise.
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    public static Boolean hasContext() {
        return context != null;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        SpringContextAccessor.context = context;
    }
}
