package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.AktivitetOgMaalgrupperFetcherService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.LandOgPostInfoFetcherService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.PersonInfoFetcherService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.LandService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        InformasjonService.class,
        VedleggService.class,
        LandService.class,
        SoknadService.class,
        InnsendtSoknadService.class,
        FaktaService.class,
        SoknadDataFletter.class,
        MigrasjonHandterer.class,
        AlternativRepresentasjonService.class,
        EttersendingService.class,
        SkjemaOppslagService.class,
        SoknadMetricsService.class,
        PersonInfoFetcherService.class,
        LandOgPostInfoFetcherService.class,
        AktivitetOgMaalgrupperFetcherService.class
})
public class ServiceConfig {

    @Bean
    public MetricsEventFactory metricsEventFactory() {
        return new MetricsEventFactory();
    }
}
