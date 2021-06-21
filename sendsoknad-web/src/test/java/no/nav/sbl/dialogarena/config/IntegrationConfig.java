package no.nav.sbl.dialogarena.config;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.Invokable;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class IntegrationConfig {

    @Bean
    public static BeanFactoryPostProcessor mockMissingBeans(){
        return beanFactory -> {
            MOCKS.clear();
            try {
                ImmutableSet<ClassPath.ClassInfo> tjenester = ClassPath
                        .from(IntegrationConfig.class.getClassLoader())
                        .getTopLevelClasses("no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig");
                System.out.println(tjenester);

                for (ClassPath.ClassInfo classInfo : tjenester) {
                    for (Method method: classInfo.load().getMethods()) {
                        Invokable<?, Object> from = Invokable.from(method);
                        if(from.isAnnotationPresent(Bean.class)){
                            Object mock = mockClass(from.getReturnType().getRawType());
                            beanFactory.registerSingleton(from.getName(), mock);
                            MOCKS.put(from.getName(), mock);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            TekstHenter mock = Mockito.mock(TekstHenter.class);
            String name = "tekstHenter";
            MOCKS.put(name, mock);
            beanFactory.registerSingleton(name, mock);
        };
    }

    private static Map<String, Object> MOCKS = new HashMap<>();

    private static Object mockClass(Class<?> type) {
        System.out.println("Mocking " + type);
        return Mockito.mock(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getMocked(String name){
        return (T) MOCKS.get(name);
    }
}
