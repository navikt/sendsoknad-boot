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
import no.nav.sbl.soknadinnsending.fillager.Filestorage;
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
    private VedleggRepository vedleggRepository;
    @Mock
    private SoknadRepository soknadRepository;
    @Mock
    private SoknadService soknadService;
    @Mock
    private SoknadDataFletter soknadDataFletter;
    @Mock
    private Filestorage filestorage;

    @InjectMocks
    private VedleggService vedleggService;

    @Before
    public void setup() throws IOException {
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

        verify(vedleggRepository).opprettEllerEndreVedlegg(eq(BEHANDLING_ID), any(Vedlegg.class), eq(kvittering));
        verify(filestorage, times(1)).store(eq(BEHANDLING_ID), any());
    }

    @Test
    public void skalOppdatereKvitteringHvisDenAlleredeFinnes() throws IOException {
        when(soknadRepository.hentSoknad(BEHANDLING_ID)).thenReturn(new WebSoknad().medBehandlingId(BEHANDLING_ID).medAktorId("aktor-1").medId(SOKNAD_ID));
        Vedlegg eksisterendeKvittering = new Vedlegg(SOKNAD_ID, null, SKJEMANUMMER_KVITTERING, LastetOpp);
        when(vedleggRepository.hentVedleggForskjemaNummer(SOKNAD_ID, null, SKJEMANUMMER_KVITTERING)).thenReturn(eksisterendeKvittering);
        byte[] kvittering = getBytesFromFile("/pdfs/minimal.pdf");

        vedleggService.lagreKvitteringSomVedlegg(BEHANDLING_ID, kvittering);

        verify(vedleggRepository).lagreVedleggMedData(eq(BEHANDLING_ID), eq(SOKNAD_ID), eq(eksisterendeKvittering.getVedleggId()), eq(eksisterendeKvittering), eq(kvittering));
        verify(filestorage, times(1)).store(eq(BEHANDLING_ID), any());
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
    public void skalIkkeGenererNyttOmVedleggFinnesFraFor() {
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


    public static byte[] getBytesFromFile(String path) throws IOException {
        try (InputStream resourceAsStream = VedleggServiceTest.class.getResourceAsStream(path)) {
            return IOUtils.toByteArray(resourceAsStream);
        }
    }
}
