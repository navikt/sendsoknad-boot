package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.LandService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
public class BusinessTestConfig {

    @Bean
    public SkjemaOppslagService skjemaOppslagService() {
        return mock(SkjemaOppslagService.class);
    }

    @Bean
    public LandService landService() {
        return mock(LandService.class);
    }

    @Bean
    public Kodeverk kodeverk() {
        return mock(Kodeverk.class);
    }

    @Bean
    public HenvendelseService henvendelseService() {
        return null;
    }

    @Bean
    public FillagerService fillagerService() {
        return null;
    }
}
