package no.nav.sbl.dialogarena.rest;

import java.util.Map;

import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
public class JerseyConfig {
    private static final Logger logger = getLogger(JerseyConfig.class);

    
    @Bean(name = "jerseyFilterRegistration")
    public FilterRegistrationBean<ServletContainer> jerseyFilter() {
        FilterRegistrationBean<ServletContainer> filter = new FilterRegistrationBean<ServletContainer>();
        filter.setFilter(new ServletContainer(new SoknadApplication()));
        filter.setOrder(0);
        filter.setName("Jersey Filter");
        filter.setInitParameters(Map.of("jersey.config.servlet.filter.contextPath","/"));
        return filter;
    }
}
