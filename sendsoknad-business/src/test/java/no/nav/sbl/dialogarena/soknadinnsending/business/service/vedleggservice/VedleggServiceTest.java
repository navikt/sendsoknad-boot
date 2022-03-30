package no.nav.sbl.dialogarena.soknadinnsending.business.service.vedleggservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.VedleggKreves;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
// Not thread safe
public class VedleggServiceTest {

    private static final long SOKNAD_ID = 1L;
    private static final String BEHANDLING_ID = "1000000ABC";

    @Mock
    private SkjemaOppslagService skjemaOppslagService;
    @Mock
    private VedleggRepository vedleggRepository;
    @Mock
    private SoknadRepository soknadRepository;
    @Mock
    private SoknadService soknadService;
    @Mock
    private SoknadDataFletter soknadDataFletter;
    @SuppressWarnings("unused")
    @Mock
    private FillagerService fillagerConnector;

    @InjectMocks
    private VedleggService vedleggService;

    @Before
    public void setup() {
        InputStream struktur = this.getClass().getResourceAsStream("/TestStruktur.xml");
        assertNotNull(struktur);
        SoknadStruktur testStruktur = JAXB.unmarshal(struktur, SoknadStruktur.class);
        when(soknadService.hentSoknadStruktur(eq("nav-1.1.1"))).thenReturn(testStruktur);
    }

    @Test
    public void skalOppretteKvitteringHvisDenIkkeFinnes() throws IOException {
        when(soknadRepository.hentSoknad(BEHANDLING_ID)).thenReturn(new WebSoknad().medBehandlingId("XXX").medAktorId("aktor-1"));
        byte[] kvittering = getBytesFromFile("/pdfs/minimal.pdf");
        vedleggService.lagreKvitteringSomVedlegg(BEHANDLING_ID, kvittering);
        verify(vedleggRepository).opprettEllerEndreVedlegg(any(Vedlegg.class), eq(kvittering));
    }

    @Test
    public void skalOppdatereKvitteringHvisDenAlleredeFinnes() throws IOException {
        when(soknadRepository.hentSoknad(BEHANDLING_ID)).thenReturn(new WebSoknad().medBehandlingId(BEHANDLING_ID).medAktorId("aktor-1").medId(SOKNAD_ID));
        Vedlegg eksisterendeKvittering = new Vedlegg(SOKNAD_ID, null, SKJEMANUMMER_KVITTERING, LastetOpp);
        when(vedleggRepository.hentVedleggForskjemaNummer(SOKNAD_ID, null, SKJEMANUMMER_KVITTERING)).thenReturn(eksisterendeKvittering);
        byte[] kvittering = getBytesFromFile("/pdfs/minimal.pdf");

        vedleggService.lagreKvitteringSomVedlegg(BEHANDLING_ID, kvittering);

        verify(vedleggRepository).lagreVedleggMedData(SOKNAD_ID, eksisterendeKvittering.getVedleggId(), eksisterendeKvittering, kvittering);
    }

    @Test
    public void skalIkkeGenerereVedleggNaarVerdiIkkeStemmer() {
        Faktum faktum = new Faktum().medKey("faktumMedToOnValue").medValue("skalIkkeGenereVedlegg");
        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true)))
                .thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                        .medFaktum(faktum));
        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(0);
    }

    @Test
    public void skalGenerereIngenVedleggOmBeggeErFalse() {
        Faktum faktum = new Faktum().medKey("toFaktumMedSammeVedlegg1").medValue("false").medFaktumId(1L);
        Faktum faktum2 = new Faktum().medKey("toFaktumMedSammeVedlegg2").medValue("false").medFaktumId(2L);
        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true)))
                .thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                        .medFaktum(faktum).medFaktum(faktum2));
        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(0);
    }

    @Test
    public void skalIkkeGenererNyttOmVedleggFinnesFraFor(){
        Faktum faktum = new Faktum().medKey("toFaktumMedSammeVedlegg1").medValue("true").medFaktumId(1L);
        Faktum faktum2 = new Faktum().medKey("toFaktumMedSammeVedlegg2").medValue("true").medFaktumId(2L);
        Vedlegg vedlegg1 = new Vedlegg().medSkjemaNummer("v4").medInnsendingsvalg(VedleggKreves);
        when(soknadDataFletter.hentSoknad(eq("123"), eq(true), eq(true)))
                .thenReturn(new WebSoknad().medskjemaNummer("nav-1.1.1")
                        .medFaktum(faktum).medFaktum(faktum2)
                        .medVedlegg(Collections.singletonList(vedlegg1)));
        List<Vedlegg> vedlegg = vedleggService.genererPaakrevdeVedlegg("123");
        assertThat(vedlegg).hasSize(1);
        assertThat(vedlegg).contains(vedlegg1);
        assertThat(vedlegg1.getInnsendingsvalg()).isEqualTo(VedleggKreves);
    }


    @Test
    public void medKodeverk_null_shouldNotThrowException() {
        vedleggService.medKodeverk(null);
    }

    @Test
    public void medKodeverk_SkjemanummerNull_shouldNotThrowException() {
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer(null);

        vedleggService.medKodeverk(vedlegg);

        assertThat(vedlegg.getTittel()).isNull();
        assertThat(vedlegg.getUrls()).isEmpty();
    }

    @Test
    public void medKodeverk_IllegalSkjemanummer_shouldNotThrowException() {
        String skjemanummer = "IllegalSkjemanummer";
        when(skjemaOppslagService.getUrl(eq(skjemanummer))).thenReturn("");
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer(skjemanummer);

        vedleggService.medKodeverk(vedlegg);

        assertThat(vedlegg.getTittel()).isNull();
        assertThat(vedlegg.getUrls()).hasSize(1);
        assertThat(vedlegg.getUrls().get("URL")).isEqualTo("");
    }

    @Test
    public void medKodeverk_VedleggWithTittelWithUrl_shouldSetTittelAndUrl() {
        String skjemanummer = "U4";
        when(skjemaOppslagService.getTittel(eq(skjemanummer))).thenReturn("Dokumentasjon av boutgifter");
        when(skjemaOppslagService.getUrl(eq(skjemanummer))).thenReturn("https://nav.no");
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer(skjemanummer);

        vedleggService.medKodeverk(vedlegg);

        assertThat(vedlegg.getTittel()).isEqualTo("Dokumentasjon av boutgifter");
        assertThat(vedlegg.getUrls()).hasSize(1);
        assertThat(vedlegg.getUrls().get("URL")).isEqualTo("https://nav.no");
    }

    @Test
    public void medKodeverk_VedleggWithTittelWithoutUrl_shouldSetTittelAndEmptyUrl() {
        String skjemanummer = "U4";
        when(skjemaOppslagService.getTittel(eq(skjemanummer))).thenReturn("Dokumentasjon av boutgifter");
        when(skjemaOppslagService.getUrl(eq(skjemanummer))).thenReturn("");
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer(skjemanummer);

        vedleggService.medKodeverk(vedlegg);

        assertThat(vedlegg.getTittel()).isEqualTo("Dokumentasjon av boutgifter");
        assertThat(vedlegg.getUrls()).hasSize(1);
        assertThat(vedlegg.getUrls().get("URL")).isEqualTo("");
    }

    @Test
    public void medKodeverk_VedleggWithoutTittelWithUrl_shouldSetUrlAndHaveNullTittel() {
        String skjemanummer = "U4";
        when(skjemaOppslagService.getTittel(eq(skjemanummer))).thenThrow(new RuntimeException("Failed to find tittel for '" + skjemanummer + "'"));
        when(skjemaOppslagService.getUrl(eq(skjemanummer))).thenReturn("https://nav.no");
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer(skjemanummer);

        vedleggService.medKodeverk(vedlegg);

        assertThat(vedlegg.getTittel()).isNull();
        assertThat(vedlegg.getUrls()).hasSize(1);
        assertThat(vedlegg.getUrls().get("URL")).isEqualTo("https://nav.no");
    }

    @Test
    public void medKodeverk_VedleggWithoutTittelWithoutUrl_shouldSetEmptyUrlAndHaveNullTittel() {
        String skjemanummer = "U4";
        when(skjemaOppslagService.getTittel(eq(skjemanummer))).thenThrow(new RuntimeException("Failed to find tittel for '" + skjemanummer + "'"));
        when(skjemaOppslagService.getUrl(eq(skjemanummer))).thenReturn("");
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer(skjemanummer);

        vedleggService.medKodeverk(vedlegg);

        assertThat(vedlegg.getTittel()).isNull();
        assertThat(vedlegg.getUrls()).hasSize(1);
        assertThat(vedlegg.getUrls().get("URL")).isEqualTo("");
    }


    @Test
    public void medKodeverk_Skjemanummer_shouldSetTittelAndEmtpyUrl() {
        String skjemanummer = "NAV 03-01.05";
        when(skjemaOppslagService.getTittel(eq(skjemanummer))).thenReturn("Orientering om rettigheter og plikter");
        when(skjemaOppslagService.getUrl(eq(skjemanummer))).thenReturn("");
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer(skjemanummer);

        vedleggService.medKodeverk(vedlegg);

        assertThat(vedlegg.getTittel()).isEqualTo("Orientering om rettigheter og plikter");
        assertThat(vedlegg.getUrls().get("URL")).isEqualTo("");
        verify(skjemaOppslagService, times(1)).getTittel(eq("NAV 03-01.05"));
        verify(skjemaOppslagService, times(1)).getUrl(eq("NAV 03-01.05"));
    }


    @Test
    public void medKodeverk_Vedlegg_shouldLookupBySkjemanummer() {
        String skjemanummer = "U4";
        when(skjemaOppslagService.getTittel(eq(skjemanummer))).thenReturn("Dokumentasjon av boutgifter");
        when(skjemaOppslagService.getUrl(eq(skjemanummer))).thenReturn("https://nav.no");
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer(skjemanummer);

        vedleggService.medKodeverk(vedlegg);

        assertThat(vedlegg.getTittel()).isEqualTo("Dokumentasjon av boutgifter");
        assertThat(vedlegg.getUrls().get("URL")).isEqualTo("https://nav.no");
        verify(skjemaOppslagService, times(1)).getTittel(eq("U4"));
        verify(skjemaOppslagService, times(1)).getUrl(eq("U4"));
    }

    @Test
    public void medKodeverk_VedleggWithExtraInfoInSkjemanummer_shouldNotLookupWithExtraInfoInSkjemanummer() {
        String skjemanummer = "U4";
        when(skjemaOppslagService.getTittel(eq(skjemanummer))).thenReturn("Dokumentasjon av boutgifter");
        when(skjemaOppslagService.getUrl(eq(skjemanummer))).thenReturn("https://nav.no");
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer("U4|hjemstedsaddresse");

        vedleggService.medKodeverk(vedlegg);

        assertThat(vedlegg.getTittel()).isEqualTo("Dokumentasjon av boutgifter");
        assertThat(vedlegg.getUrls().get("URL")).isEqualTo("https://nav.no");
        verify(skjemaOppslagService, times(1)).getTittel(eq("U4"));
        verify(skjemaOppslagService, times(1)).getUrl(eq("U4"));
    }

    public static byte[] getBytesFromFile(String path) throws IOException {
        InputStream resourceAsStream = VedleggServiceTest.class.getResourceAsStream(path);
        return IOUtils.toByteArray(resourceAsStream);
    }
}
