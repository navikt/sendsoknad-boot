package no.nav.sbl.dialogarena.soknadinnsending.business.service.vedleggservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.XmlService;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;

@Configuration
public class VedleggServiceIntegrationMockContext {

    @Bean
    public FaktaService faktaService() {
        return mock(FaktaService.class);
    }

    @Bean("soknadInnsendingRepository")
    public SoknadRepository lokalDb() {
        return mock(SoknadRepository.class);
    }

    @Bean
    public WebSoknadConfig config() {
        return mock(WebSoknadConfig.class);
    }

    @Bean
    public ApplicationContext applicationContext() {
        return mock(ApplicationContext.class);
    }

    @Bean
    public TekstHenter tekstHenter() { return mock(TekstHenter.class); }

    @Bean
    public DataSource dataSource() {
        return mock(DataSource.class);
    }

    @Bean
    public XmlService xmlService() {
        return mock(XmlService.class);
    }

    @Bean
    public SoknadDataFletter soknadDataFletter() {
        return mock(SoknadDataFletter.class);
    }

    @Bean
    public AlternativRepresentasjonService alternativRepresentasjonService() {
        return mock(AlternativRepresentasjonService.class);
    }

    @Bean
    public SoknadService soknadService(){
        return mock(SoknadService.class);
    }

    @Bean
    public EttersendingService ettersendingService(){
        return mock(EttersendingService.class);
    }

    @Bean("vedleggRepository")
    public VedleggRepository vedleggRepository() {
        return mock(VedleggRepositoryJdbc.class);
    }

    @Bean
    public SoknadMetricsService metricsService() {
        return mock(SoknadMetricsService.class);
    }
}
