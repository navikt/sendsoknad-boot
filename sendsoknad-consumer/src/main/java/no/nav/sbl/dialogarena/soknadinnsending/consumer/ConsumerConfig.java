package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.kodeverk.KodeverkService;
import no.nav.sbl.dialogarena.kodeverk.config.KodeverkSpringConfig;
import no.nav.sbl.dialogarena.kodeverk.config.OkHttpClientConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.EpostService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.DkifKrrProxyClient;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.*;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import static java.lang.System.setProperty;

@Configuration
@EnableCaching
@Import({
        PersonService.class,
        EpostService.class,
        ConsumerConfig.WsServices.class,
        KodeverkSpringConfig.KodeverkRestServices.class,
        PersonaliaFletter.class
})
public class ConsumerConfig {

    //Må godta så store xml-payloads pga Kodeverk postnr
    static {
        setProperty("org.apache.cxf.staxutils.innerElementCountThreshold", "70000");
    }

    @Configuration
    @Profile("!integration")
    @Import({
            ArbeidWSConfig.class,
            OrganisasjonWSConfig.class,
            BrukerProfilWSConfig.class,
            KodeverkWSConfig.class,
            PersonWSConfig.class,
            MaalgruppeWSConfig.class,
            SakOgAktivitetWSConfig.class,
            DkifKrrProxyClient.class
    })
    public static class WsServices {
    }
}
