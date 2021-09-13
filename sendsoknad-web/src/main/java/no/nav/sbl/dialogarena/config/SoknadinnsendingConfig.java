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

import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.boot.web.embedded.jetty.ConfigurableJettyWebServerFactory;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
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
	/*
	WebAppContext test() {
		WebAppContext test = new WebAppContext();
		ConstraintSecurityHandler handler = new ConstraintSecurityHandler();
		handler.setLoginService(new JAASLoginService());
		handler.setAuthenticator(null);
		JettyWebXmlConfiguration conf;
		conf.
		OpenAMLOginM
		JAASLoginService
	}
	
	
	@Bean
	public ConfigurableServletWebServerFactory webServerFactory() 
	{
		ConfigurableJettyWebServerFactory test;
		
		JettyServerCustomizer cust;
		cust.
		
		//Constraintsec
	    JettyServletWebServerFactory factory = new JettyServletWebServerFactory();
	    JettyWebXmlConfiguration config = new JettyWebXmlConfiguration();
	    WebAppContext context = new WebAppContext();
	    SecurityHandler securityHandler = context.getSecurityHandler();
	    JAASLoginService jaasLoginService = new JAASLoginService("OpenAM Realm");
	    jaasLoginService.setLoginModuleName("openam");
	    securityHandler.setLoginService(jaasLoginService);
	    context.setSecurityHandler(null);
	    
	    config.configure(null);
	    
	    SecurityHandler.getCurrentSecurityHandler().setLo
	    
	    factory.
	    factory.setC
	    Constrain
	    return factory;
	}
		
	
	*/
	
}
