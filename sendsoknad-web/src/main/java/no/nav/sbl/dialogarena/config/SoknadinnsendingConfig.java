package no.nav.sbl.dialogarena.config;

import no.nav.modig.presentation.logging.session.MDCFilter;
import no.nav.modig.security.filter.OpenAMLoginFilter;
import no.nav.sbl.dialogarena.common.suspend.IsAliveServlet;
import no.nav.sbl.dialogarena.selftest.SelftestServlet;
import no.nav.sbl.dialogarena.sikkerhet.HeaderFilter;
import no.nav.sbl.dialogarena.soknadinnsending.business.BusinessConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadInnsendingDBConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfig;

import javax.servlet.Filter;
import javax.servlet.ServletRegistration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.web.filter.CharacterEncodingFilter;

@EnableAspectJAutoProxy
@Configuration
@Import({
        ApplicationConfig.class,
        BusinessConfig.class,
        CacheConfig.class,
        ConsumerConfig.class,
        SoknadInnsendingDBConfig.class,
        HandlebarsHelperConfig.class,
        MetricsConfig.class,
        SikkerhetsConfig.class
})
@ComponentScan(basePackages = "no.nav.sbl.dialogarena.rest")
public class SoknadinnsendingConfig {
	
	@Bean(name = "IsAlive")
	public ServletRegistrationBean<IsAliveServlet> isAliveServlet() {
		return new ServletRegistrationBean<IsAliveServlet>(new IsAliveServlet(), "/internal/isAlive");
	}
	
	@Bean(name = "SelfTest")
	public ServletRegistrationBean<SelftestServlet> selfTestServlet() {
		return new ServletRegistrationBean<SelftestServlet>(new SelftestServlet(), "/internal/selftest");
	}
	
	@Bean
	public FilterRegistrationBean<MDCFilter> mdcFilter() {
			MDCFilter mdcFilter = new MDCFilter();
			FilterRegistrationBean<MDCFilter> register = new FilterRegistrationBean<MDCFilter>();
			register.setFilter(mdcFilter);
			register.addUrlPatterns("/*");
			register.setAsyncSupported(true);
			register.setName("MdcFilter");
			return register;
	}
		
	
	
	
}
