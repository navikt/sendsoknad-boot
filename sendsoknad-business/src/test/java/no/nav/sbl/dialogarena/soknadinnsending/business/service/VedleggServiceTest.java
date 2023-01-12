package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SoknadCannotBeChangedException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.pdfutility.PdfUtilities;
import no.nav.sbl.soknadinnsending.fillager.Filestorage;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.VedleggKreves;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VedleggServiceTest {
    private static final String BEHANDLINGSID = "apabepa";
    @Mock
    private SoknadRepository soknadRepository;
    @Mock
    private VedleggRepository vedleggRepository;
    @Mock
    private SoknadService soknadService;
    @Mock
    private SoknadDataFletter soknadDataFletter;
    @Mock
    private Filestorage filestorage;

    @InjectMocks
    private VedleggService vedleggService;


    @Test
    public void skalAKonvertereFilerVedOpplasting() throws IOException {
        byte[] data = PdfUtilities.createPDFFromImage(getBytesFromFile("/images/bilde.jpg"));
        Vedlegg vedlegg = new Vedlegg()
                .medVedleggId(1L)
                .medSoknadId(1L)
                .medFaktumId(1L)
                .medSkjemaNummer("1")
                .medNavn(null)
                .medStorrelse(1L)
                .medAntallSider(1)
                .medOpprettetDato(DateTime.now().getMillis())
                .medInnsendingsvalg(VedleggKreves);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        when(vedleggRepository.opprettEllerEndreVedlegg(anyString(), any(Vedlegg.class), captor.capture())).thenReturn(11L);

        long id = vedleggService.lagreVedlegg(vedlegg, data, "");

        assertEquals(11L, id);
        verify(filestorage, times(1)).store(anyString(), anyList());
    }

    @Test
    public void skalGenererForhandsvisning_liteVedlegg() {
        byte[] bytes = vedleggService.lagForhandsvisning(20631L, 0);
        byte[] bytes2 = vedleggService.lagForhandsvisning(20631L, 0);

        assertEquals(bytes.length, bytes2.length);
    }

    @Test
    public void skalGenererForhandsvisning_stortVedlegg() {
        long start = System.currentTimeMillis();
        byte[] bytes = vedleggService.lagForhandsvisning(1L, 0);
        long slutt = System.currentTimeMillis();
        byte[] bytes2 = vedleggService.lagForhandsvisning(1L, 0);
        long slutt2 = System.currentTimeMillis();

        assertEquals(bytes.length, bytes2.length);
        assertTrue(slutt2 - slutt <= slutt - start);
    }

    @Test
    public void skalGenererForhandsvisning_pdfMedSpesiellFont() throws IOException {
        byte[] pdfBytes = getBytesFromFile("/pdfs/navskjema.pdf");
        when(vedleggRepository.hentVedleggData(any())).thenReturn(pdfBytes);
        long start = System.currentTimeMillis();
        byte[] bytes = vedleggService.lagForhandsvisning(20631L, 0);
        long slutt = System.currentTimeMillis();
        byte[] bytes2 = vedleggService.lagForhandsvisning(20631L, 0);
        long slutt2 = System.currentTimeMillis();

        assertEquals(bytes.length, bytes2.length);
        assertTrue(slutt2 - slutt <= slutt - start);

        vedleggService.lagForhandsvisning(20631L, 0);
    }

    @Test
    public void skalHenteVedlegg() {
        vedleggService.hentVedlegg(1L, false);
        verify(vedleggRepository).hentVedlegg(1L);

        vedleggService.hentVedlegg(1L, true);
        verify(vedleggRepository).hentVedleggMedInnhold(1L);
    }

    @Test
    public void skalLagreEnPdfMedFlereSiderSomEttDokument() throws IOException {
        byte[] data = getBytesFromFile("/pdfs/navskjema.pdf");
        Vedlegg vedlegg = new Vedlegg()
                .medVedleggId(1L)
                .medSoknadId(1L)
                .medFaktumId(1L)
                .medSkjemaNummer("1")
                .medNavn("")
                .medStorrelse(1L)
                .medAntallSider(1)
                .medOpprettetDato(DateTime.now().getMillis())
                .medInnsendingsvalg(VedleggKreves);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        when(vedleggRepository.opprettEllerEndreVedlegg(anyString(), any(Vedlegg.class), captor.capture()))
                .thenReturn(10L, 11L, 12L, 13L, 14L);

        long id = vedleggService.lagreVedlegg(vedlegg, data, "");

        assertTrue(PdfUtilities.isPDF(captor.getValue()));
        assertEquals(10L, id);
        verify(filestorage, times(1)).store(anyString(), anyList());
    }

    @Test
    public void skalGenerereVedleggFaktum() throws IOException {
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer("L6").medSoknadId(1L).medVedleggId(2L);
        byte[] bytes = getBytesFromFile("/pdfs/minimal.pdf");
        Vedlegg vedleggSjekk = new Vedlegg()
                .medSkjemaNummer("L6")
                .medInnsendingsvalg(LastetOpp)
                .medSoknadId(1L)
                .medAntallSider(1)
                .medVedleggId(2L)
                .medFillagerReferanse(vedlegg.getFillagerReferanse())
                .medData(bytes)
                .medStorrelse((long) bytes.length);

        when(vedleggRepository.hentVedlegg(2L)).thenReturn(vedlegg);
        when(vedleggRepository.hentVedleggUnderBehandling(BEHANDLINGSID, vedlegg.getFillagerReferanse())).thenReturn(singletonList(new Vedlegg().medVedleggId(10L)));
        when(vedleggRepository.hentVedleggData(10L)).thenReturn(bytes);
        when(soknadRepository.hentSoknad(BEHANDLINGSID)).thenReturn(new WebSoknad().medBehandlingId(BEHANDLINGSID).medAktorId("234").medId(1L).medStatus(SoknadInnsendingStatus.UNDER_ARBEID));

        vedleggService.genererVedleggFaktum(BEHANDLINGSID, 2L);

        verify(vedleggRepository).lagreVedleggMedData(eq(BEHANDLINGSID), eq(1L), eq(2L), eq(vedleggSjekk), eq(bytes));
        verify(filestorage).store(eq(BEHANDLINGSID), any());
    }

    @Test
    public void generereVedleggFaktumFeilerHvisIkkeSoknadUnderArbeid() throws IOException {
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer("L6").medSoknadId(1L).medVedleggId(2L);
        byte[] bytes = getBytesFromFile("/pdfs/minimal.pdf");
        Vedlegg vedleggSjekk = new Vedlegg()
                .medSkjemaNummer("L6")
                .medInnsendingsvalg(LastetOpp)
                .medSoknadId(1L)
                .medAntallSider(1)
                .medVedleggId(2L)
                .medFillagerReferanse(vedlegg.getFillagerReferanse())
                .medData(bytes)
                .medStorrelse((long) bytes.length);

        when(vedleggRepository.hentVedlegg(2L)).thenReturn(vedlegg);
        when(soknadRepository.hentSoknad(BEHANDLINGSID)).thenReturn(new WebSoknad().medBehandlingId(BEHANDLINGSID).medAktorId("234").medId(1L).medStatus(SoknadInnsendingStatus.FERDIG));

        assertThrows(SoknadCannotBeChangedException.class, () -> vedleggService.genererVedleggFaktum(BEHANDLINGSID, 2L));
    }

    @Test
    public void skalSletteVedlegg() {
        when(soknadService.hentSoknadFraLokalDb(1L)).thenReturn(new WebSoknad().medBehandlingId("123").medAktorId("234").medDelstegStatus(OPPRETTET).medId(1L));
        when(vedleggService.hentVedlegg(2L, false)).thenReturn(new Vedlegg().medSoknadId(1L));

        vedleggService.slettVedlegg(2L);

        verify(vedleggRepository).slettVedlegg(1L, 2L);
        verify(soknadRepository).settDelstegstatus(1L, SKJEMA_VALIDERT);
    }

    @Test
    public void skalLagreVedlegg() {
        when(soknadService.hentSoknadFraLokalDb(11L)).thenReturn(new WebSoknad().medBehandlingId(BEHANDLINGSID).medDelstegStatus(OPPRETTET));
        Vedlegg vedlegg = new Vedlegg().medVedleggId(1L).medSoknadId(11L).medData("En tekst".getBytes());

        vedleggService.lagreVedlegg(vedlegg);

        verify(vedleggRepository).lagreVedlegg(eq(BEHANDLINGSID), eq(11L), eq(1L), eq(vedlegg));
    }

    @Test(expected = SendSoknadException.class)
    public void skalIkkeKunneLagreVedleggMedNegradertInnsendingsStatus() {
        Vedlegg opplastetVedlegg = new Vedlegg()
                .medVedleggId(1L)
                .medOpprinneligInnsendingsvalg(LastetOpp)
                .medInnsendingsvalg(Vedlegg.Status.SendesIkke);

        vedleggService.lagreVedlegg(opplastetVedlegg);

        verify(vedleggRepository, never()).lagreVedlegg(anyString(), any(), any(), any());
    }

    @Test
    public void skalKunneLagreVedleggMedSammeInnsendinsStatus() {
        when(soknadService.hentSoknadFraLokalDb(11L)).thenReturn(new WebSoknad().medBehandlingId(BEHANDLINGSID).medDelstegStatus(OPPRETTET));
        Vedlegg opplastetVedlegg = new Vedlegg()
                .medVedleggId(1L)
                .medOpprinneligInnsendingsvalg(LastetOpp)
                .medInnsendingsvalg(LastetOpp)
                .medSoknadId(11L)
                .medData("En tekst".getBytes());

        vedleggService.lagreVedlegg(opplastetVedlegg);

        verify(vedleggRepository).lagreVedlegg(eq(BEHANDLINGSID), eq(11L), eq(1L), eq(opplastetVedlegg));
    }

    @Test
    public void skalIkkeSetteDelstegDersomVedleggLagresPaaEttersending() {
        when(soknadService.hentSoknadFraLokalDb(11L)).thenReturn(new WebSoknad().medBehandlingId(BEHANDLINGSID).medDelstegStatus(ETTERSENDING_OPPRETTET));
        Vedlegg opplastetVedlegg = new Vedlegg()
                .medVedleggId(1L)
                .medOpprinneligInnsendingsvalg(LastetOpp)
                .medInnsendingsvalg(LastetOpp)
                .medSoknadId(11L)
                .medData("En tekst".getBytes());

        vedleggService.lagreVedlegg(opplastetVedlegg);

        verify(vedleggRepository).lagreVedlegg(eq(BEHANDLINGSID), eq(11L), eq(1L), eq(opplastetVedlegg));
        verify(soknadRepository, never()).settDelstegstatus(11L, SKJEMA_VALIDERT);
    }

    @Test
    public void skalIkkeLageDuplikaterAvVedleggPaaEttersending() {
        Faktum faktum = new Faktum().medKey("ekstraVedlegg").medFaktumId(12L).medValue("true");
        Vedlegg ekstraVedlegg = new Vedlegg().medVedleggId(1L).medFaktumId(12L).medSkjemaNummer("N6").medInnsendingsvalg(VedleggKreves);
        List<Vedlegg> vedlegg = singletonList(ekstraVedlegg);

        when(soknadDataFletter.hentSoknad(BEHANDLINGSID, true, true)).thenReturn(new WebSoknad().medDelstegStatus(ETTERSENDING_OPPRETTET).medFaktum(faktum).medVedlegg(vedlegg));
        when(vedleggRepository.hentVedlegg(BEHANDLINGSID)).thenReturn(vedlegg);

        List<Vedlegg> paakrevdeVedlegg = vedleggService.genererPaakrevdeVedlegg(BEHANDLINGSID);

        assertEquals(1, paakrevdeVedlegg.size());
        assertEquals(ekstraVedlegg, paakrevdeVedlegg.get(0));
    }

    @Test
    public void skalKunneLagreVedleggMedOppgradertInnsendingsStatus() {
        when(soknadService.hentSoknadFraLokalDb(11L)).thenReturn(new WebSoknad().medBehandlingId(BEHANDLINGSID).medDelstegStatus(OPPRETTET));
        Vedlegg vedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(Vedlegg.Status.SendesIkke).medSoknadId(11L).medData("En tekst".getBytes());

        vedlegg.setInnsendingsvalg(Vedlegg.Status.SendesSenere);
        vedleggService.lagreVedlegg(vedlegg);
        verify(vedleggRepository).lagreVedlegg(eq(BEHANDLINGSID), eq(11L), eq(1L), eq(vedlegg));
    }

    @Test(expected = SendSoknadException.class)
    public void skalIkkeKunneLagreVedleggMedPrioritetMindreEllerLik1() {
        Vedlegg vedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(VedleggKreves);

        vedlegg.setInnsendingsvalg(VedleggKreves);
        vedleggService.lagreVedlegg(vedlegg);
        verify(vedleggRepository, never()).lagreVedlegg(anyString(), any(), any(), any());
    }

    public static byte[] getBytesFromFile(String path) throws IOException {
        try (InputStream resourceAsStream = VedleggServiceTest.class.getResourceAsStream(path)) {
            return IOUtils.toByteArray(resourceAsStream);
        }
    }
}
