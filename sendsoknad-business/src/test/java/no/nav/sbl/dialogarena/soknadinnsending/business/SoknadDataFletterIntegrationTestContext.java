package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.RepositoryTestSupport;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.TestSupport;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggFraHenvendelsePopulator;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.BrukernotifikasjonService;
import no.nav.sbl.soknadinnsending.fillager.Filestorage;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.time.Clock;

import static org.mockito.Mockito.mock;

@Import(value = {DatabaseTestContext.class})
@EnableTransactionManagement()
@Configuration
public class SoknadDataFletterIntegrationTestContext {

    @Autowired
    private DataSource dataSource;

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }


    @Bean
    public SoknadDataFletter fletter(ApplicationContext context) {
        return new SoknadDataFletter(context, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,null,
                "true", "true");
    }

    @Bean
    public AlternativRepresentasjonService alternativRepresentasjonService() {
        return new AlternativRepresentasjonService(null, null);
    }

    @Bean
    public HenvendelseService henvendelseService() {
        return new HenvendelseService(null, null, null);
    }

    @Bean
    public SendSoknadPortType sendSoknadEndpoint() {
        return mock(SendSoknadPortType.class);
    }

    @Bean
    public SendSoknadPortType sendSoknadSelftestEndpoint() {
        return mock(SendSoknadPortType.class);
    }

    @Bean
    public HenvendelsePortType henvendelseEndpoint() {
        return mock(HenvendelsePortType.class);
    }

    @Bean
    public FillagerService fillagerService() {
        return new FillagerService(null, null);
    }

    @Bean
    public FilLagerPortType fillagerEndpoint() {
        return mock(FilLagerPortType.class);
    }

    @Bean
    public FilLagerPortType fillagerSelftestEndpoint() {
        return mock(FilLagerPortType.class);
    }

    @Bean
    public VedleggService vedleggService(@Qualifier("soknadInnsendingRepository") SoknadRepository repository,
                                         @Qualifier("vedleggRepository") VedleggRepository vedleggRepository) {
        return new VedleggService(repository, vedleggRepository, null, null,
                null, null, null, null, null, "true");
    }

    @Bean
    public VedleggFraHenvendelsePopulator vedlegFraHenvendelsePopulator(
            @Qualifier("vedleggRepository") VedleggRepository vedleggRepository
    ) {
        return new VedleggFraHenvendelsePopulator(vedleggRepository);
    }

    @Bean
    public SoknadRepository soknadInnsendingRepository() {
        return new SoknadRepositoryJdbc();
    }

    @Bean
    public HendelseRepository hendelseRepository() {
        return new HendelseRepositoryJdbc();
    }

    @Bean
    public VedleggRepository vedleggRepository() {
        return new VedleggRepositoryJdbc();
    }

    @Bean
    public RepositoryTestSupport testSupport() {
        return new TestSupport(dataSource);
    }

    @Bean
    public SkjemaOppslagService skjemaOppslagService() {
        return mock(SkjemaOppslagService.class);
    }

    @Bean
    public SoknadService soknadService() {
        return new SoknadService(null, null, null, null, null, null, null, null, null);
    }

    @Bean
    public WebSoknadConfig webSoknadConfig() {
        return new WebSoknadConfig(null, null);
    }

    @Bean
    public FaktaService faktaService(@Qualifier("soknadInnsendingRepository") SoknadRepository repository, @Qualifier("vedleggRepository") VedleggRepository vedleggRepository) {
        return new FaktaService(repository, vedleggRepository);
    }

    @Bean
    public TekstHenter tekstHenter() {
        return new TekstHenter();
    }

    @Bean
    public EttersendingService ettersendingService() {
        return new EttersendingService(null, null, null, null, null,null,null);
    }

    @Bean
    public SoknadMetricsService metricsService() {
        return mock(SoknadMetricsService.class);
    }

    @Bean
    public LegacyInnsendingService legacyInnsendingService() {
        return mock(LegacyInnsendingService.class);
    }

    @Bean
    public InnsendingService innsendingService() {
        return mock(InnsendingService.class);
    }

    @Bean
    public Filestorage filestorage() {
        return mock(Filestorage.class);
    }

    @Bean
    public BrukernotifikasjonService brukernotifikasjonService() { return mock(BrukernotifikasjonService.class);}
}
