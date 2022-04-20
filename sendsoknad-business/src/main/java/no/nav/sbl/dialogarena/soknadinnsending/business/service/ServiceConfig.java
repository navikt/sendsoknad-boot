package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.AktivitetOgMaalgrupperFetcherService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.LandOgPostInfoFetcherService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.PersonInfoFetcherService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.LandService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.soknadinnsending.config.InnsendingSpringConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        InformasjonService.class,
        VedleggService.class,
        VedleggHentOgPersistService.class,
        VedleggFraHenvendelsePopulator.class,
        LandService.class,
        SoknadService.class,
        InnsendtSoknadService.class,
        FaktaService.class,
        SoknadDataFletter.class,
        AlternativRepresentasjonService.class,
        EttersendingService.class,
        SkjemaOppslagService.class,
        InnsendingService.class,
        LegacyInnsendingService.class,
        InnsendingSpringConfig.class,
        SoknadMetricsService.class,
        PersonInfoFetcherService.class,
        LandOgPostInfoFetcherService.class,
        AktivitetOgMaalgrupperFetcherService.class
})
public class ServiceConfig {
}
