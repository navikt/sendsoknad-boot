package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.rest.actions.SoknadActions;
import no.nav.sbl.dialogarena.rest.utils.PDFService;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.service.EmailService;
import no.nav.sbl.dialogarena.service.HtmlGenerator;
import no.nav.sbl.dialogarena.soknadinnsending.business.BusinessTestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.MetricsEventFactory;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadMetricsService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mock;

@Configuration
@Import({ DummyHolderConfig.class, BusinessTestConfig.class })
public class SoknadActionsTestConfig {

    @Bean
    public TekstHenter tekster() {
        return mock(TekstHenter.class);
    }

    @Bean
    public EmailService emailService() {
        return mock(EmailService.class);
    }

    @Bean
    public SoknadService soknadService() {
        return mock(SoknadService.class);
    }

    @Bean
    public VedleggService vedleggService() {
        return mock(VedleggService.class);
    }

    @Bean
    public HtmlGenerator pdfTemplate() {
        return mock(HtmlGenerator.class);
    }

    @Bean
    public SoknadActions soknadActions() {
        return new SoknadActions();
    }

    @Bean
    public PDFService pdfService() {
        return new PDFService();
    }

    @Bean
    public WebSoknadConfig config() {
        return mock(WebSoknadConfig.class);
    }

    @Bean
    public SoknadMetricsService soknadMetricsService() {
        return mock(SoknadMetricsService.class);
    }

    @Bean
    public MetricsEventFactory metricsEventFactory() {
        return new MetricsEventFactory();
    }
}
