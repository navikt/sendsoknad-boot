package no.nav.sbl.dialogarena.soknadinnsending.business.service.vedleggservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.soknadinnsending.fillager.Filestorage;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Clock;

import static org.mockito.Mockito.mock;

@Configuration
@Import({VedleggServiceIntegrationMockContext.class})
public class VedleggServiceIntegrationContext {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public VedleggService vedleggService(@Qualifier("soknadInnsendingRepository") SoknadRepository repository,
                                         @Qualifier("vedleggRepository") VedleggRepository vedleggRepository,
                                         SoknadService soknadService, SoknadDataFletter soknadDataFletter,
                                         FillagerService fillagerService, FaktaService faktaService,
                                         TekstHenter tekstHenter, Filestorage filestorage) {
        return new VedleggService(repository, vedleggRepository, soknadService, soknadDataFletter,
                fillagerService, faktaService, tekstHenter, filestorage, "true");
    }

    @Bean
    public FaktaService faktaService(
            @Qualifier("soknadInnsendingRepository") SoknadRepository repository,
            @Qualifier("vedleggRepository") VedleggRepository vedleggRepository
    ) {
        return new FaktaService(repository, vedleggRepository);
    }

    @Bean
    public HenvendelsePortType henvendelseEndpoint() {
        return mock(HenvendelsePortType.class);
    }

    @Bean
    public HendelseRepository hendelseRepository() {
        return new HendelseRepositoryJdbc();
    }

    @Bean
    public SkjemaOppslagService skjemaOppslagService() {
        return mock(SkjemaOppslagService.class);
    }

    @Bean
    public Filestorage filestorage() {
        return mock(Filestorage.class);
    }
}
