package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.interceptor.MDCInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    private final MDCInterceptor mdcInterceptor;

    @Autowired
    public ResourceConfig(MDCInterceptor mdcInterceptor) {
        this.mdcInterceptor = mdcInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mdcInterceptor);
    }
}
