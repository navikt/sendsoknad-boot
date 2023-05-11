package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.soknadinnsending.business.aktivitetbetalingsplan.AktivitetBetalingsplanBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.ArbeidsforholdBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.CheckApplicationArchivingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.GamleSoknaderSletterScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.SlettFilerTilInnsendteSoknader;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.kafka.KafkaMessageReader;
import no.nav.sbl.dialogarena.soknadinnsending.business.kafka.KafkaReading;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BarnBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.ServiceConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        GamleSoknaderSletterScheduler.class,
        CheckApplicationArchivingStatus.class,
        SlettFilerTilInnsendteSoknader.class,
        DbConfig.class,
        AktivitetService.class,
        MaalgrupperService.class,
        PersonaliaBolk.class,
        AktivitetBetalingsplanBolk.class,
        BarnBolk.class,
        ConsumerConfig.class,
        ArbeidsforholdBolk.class,
        ServiceConfig.class,
        ServicesApplicationConfig.class,
        WebSoknadConfig.class,
        ArbeidsforholdService.class,
        ArbeidsforholdTransformer.class,
})
public class BusinessConfig {
}
