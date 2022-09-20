package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggHentOgPersistService.medKodeverk;
import static org.assertj.core.api.Assertions.assertThat;

public class VedleggHentOgPersistServiceTest {

    @Before
    public void setup() throws IOException {
        SkjemaOppslagService.initializeFromOldResult();
    }


    @Test
    public void medKodeverk_null_shouldNotThrowException() {
        medKodeverk(null);
    }

    @Test
    public void medKodeverk_SkjemanummerNull_shouldNotThrowException() {
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer(null);

        medKodeverk(vedlegg);

        assertThat(vedlegg.getTittel()).isNull();
        assertThat(vedlegg.getUrls()).isEmpty();
    }

    @Test
    public void medKodeverk_IllegalSkjemanummer_shouldNotThrowException() {
        String skjemanummer = "IllegalSkjemanummer";
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer(skjemanummer);

        medKodeverk(vedlegg);

        assertThat(vedlegg.getTittel()).isNull();
    }

    @Test
    public void medKodeverk_VedleggWithTittelWithoutUrl_shouldSetTittelAndEmptyUrl() {
        String skjemanummer = "U4";
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer(skjemanummer);

        medKodeverk(vedlegg);

        assertThat(vedlegg.getTittel()).isEqualTo("Dokumentasjon av boutgifter");
        assertThat(vedlegg.getUrls()).hasSize(1);
        assertThat(vedlegg.getUrls().get("URL")).isEqualTo("");
    }

    @Test
    public void medKodeverk_Skjemanummer_shouldSetTittelAndEmtpyUrl() {
        String skjemanummer = "NAV 11-12.10";
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer(skjemanummer);

        medKodeverk(vedlegg);

        assertThat(vedlegg.getTittel()).isEqualTo("Kjøreliste for godkjent bruk av egen bil");
        assertThat(vedlegg.getUrls().get("URL")).startsWith("https://cdn.sanity.io/");
    }

    @Test
    public void medKodeverk_VedleggWithExtraInfoInSkjemanummer_shouldNotLookupWithExtraInfoInSkjemanummer() {
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer("U4|hjemstedsaddresse");

        medKodeverk(vedlegg);

        assertThat(vedlegg.getTittel()).isEqualTo("Dokumentasjon av boutgifter");
    }
}
