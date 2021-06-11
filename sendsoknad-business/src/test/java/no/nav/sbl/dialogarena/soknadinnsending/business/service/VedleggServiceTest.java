package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.pdfutility.PdfUtilities;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.VedleggKreves;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VedleggServiceTest {
    @Mock
    private SoknadRepository soknadRepository;
    @Mock
    private VedleggRepository vedleggRepository;
    @Mock
    private SoknadService soknadService;
    @Mock
    private SoknadDataFletter soknadDataFletter;
    @Mock
    private FillagerService fillagerService;

    @InjectMocks
    private VedleggService vedleggService;

    @Test
    public void skalAKonvertereFilerVedOpplasting() throws IOException {
        Vedlegg vedlegg = new Vedlegg()
                .medVedleggId(1L)
                .medSoknadId(1L)
                .medFaktumId(1L)
                .medSkjemaNummer("1")
                .medNavn(null)
                .medStorrelse(1L)
                .medAntallSider(1)
                .medFillagerReferanse(null)
                .medData(PdfUtilities.createPDFFromImage(getBytesFromFile("/images/bilde.jpg")))
                .medOpprettetDato(DateTime.now().getMillis())
                .medInnsendingsvalg(VedleggKreves);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        when(vedleggRepository.opprettEllerEndreVedlegg(any(Vedlegg.class), captor.capture())).thenReturn(11L);

        long id = vedleggService.lagreVedlegg(vedlegg);
        assertEquals(11L, id);
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
        String filnavn = "navskjema";
        byte[] pdfBytes = getBytesFromFile("/pdfs/"+filnavn+".pdf");
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
        Vedlegg vedlegg = new Vedlegg()
                .medVedleggId(1L)
                .medSoknadId(1L)
                .medFaktumId(1L)
                .medSkjemaNummer("1")
                .medNavn("")
                .medStorrelse(1L)
                .medAntallSider(1)
                .medFillagerReferanse(null)
                .medData(getBytesFromFile("/pdfs/navskjema.pdf"))
                .medOpprettetDato(DateTime.now().getMillis())
                .medInnsendingsvalg(VedleggKreves);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        when(vedleggRepository.opprettEllerEndreVedlegg(any(Vedlegg.class), captor.capture())).thenReturn(10L, 11L, 12L, 13L, 14L);

        long id = vedleggService.lagreVedlegg(vedlegg);
        assertTrue(PdfUtilities.isPDF(captor.getValue()));
        assertEquals(10L, id);
    }

    @Test
    public void skalGenerereVedleggFaktum() throws IOException {
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer("L6").medSoknadId(1L).medVedleggId(2L);
        byte[] bytes = getBytesFromFile("/pdfs/minimal.pdf");
        Vedlegg vedleggSjekk = new Vedlegg().medSkjemaNummer("L6").medInnsendingsvalg(LastetOpp).medSoknadId(1L).medAntallSider(1).medVedleggId(2L).medFillagerReferanse(vedlegg.getFillagerReferanse()).medData(bytes);
        when(vedleggRepository.hentVedlegg(2L)).thenReturn(vedlegg);
        when(vedleggRepository.hentVedleggUnderBehandling("ABC", vedlegg.getFillagerReferanse())).thenReturn(Collections.singletonList(new Vedlegg().medVedleggId(10L)));
        when(vedleggRepository.hentVedleggData(10L)).thenReturn(bytes);
        when(soknadRepository.hentSoknad("ABC")).thenReturn(new WebSoknad().medBehandlingId("ABC").medAktorId("234").medId(1L));
        vedleggService.genererVedleggFaktum("ABC", 2L);
        vedleggSjekk.setData(vedlegg.getData());
        vedleggSjekk.medStorrelse((long) vedlegg.getData().length);
        verify(vedleggRepository).lagreVedleggMedData(1L, 2L, vedleggSjekk);
        verify(fillagerService).lagreFil(eq("ABC"), eq(vedleggSjekk.getFillagerReferanse()), eq("234"), any(InputStream.class));
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
        when(soknadService.hentSoknadFraLokalDb(11L)).thenReturn(new WebSoknad().medDelstegStatus(OPPRETTET));
        Vedlegg vedlegg = new Vedlegg().medVedleggId(1L).medSoknadId(11L);
        vedleggService.lagreVedlegg(1L, vedlegg);
        verify(vedleggRepository).lagreVedlegg(11L, 1L, vedlegg);
    }

    @Test(expected = SendSoknadException.class)
    public void skalIkkeKunneLagreVedleggMedNegradertInnsendingsStatus() {
        Vedlegg opplastetVedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(LastetOpp);

        opplastetVedlegg.setInnsendingsvalg(Vedlegg.Status.SendesIkke);
        vedleggService.lagreVedlegg(1L, opplastetVedlegg);
        verify(vedleggRepository, never()).lagreVedlegg(11L, 1L, opplastetVedlegg);
    }

    @Test
    public void skalKunneLagreVedleggMedSammeInnsendinsStatus() {
        when(soknadService.hentSoknadFraLokalDb(11L)).thenReturn(new WebSoknad().medDelstegStatus(OPPRETTET));
        Vedlegg opplastetVedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(LastetOpp).medSoknadId(11L);
        vedleggService.lagreVedlegg(1L, opplastetVedlegg);

        opplastetVedlegg.setInnsendingsvalg(LastetOpp);
        verify(vedleggRepository).lagreVedlegg(11L, 1L, opplastetVedlegg);
    }

    @Test
    public void skalIkkeSetteDelstegDersomVedleggLagresPaaEttersending() {
        when(soknadService.hentSoknadFraLokalDb(11L)).thenReturn(new WebSoknad().medDelstegStatus(ETTERSENDING_OPPRETTET));
        Vedlegg opplastetVedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(LastetOpp).medSoknadId(11L);

        opplastetVedlegg.setInnsendingsvalg(LastetOpp);
        vedleggService.lagreVedlegg(1L, opplastetVedlegg);
        verify(vedleggRepository).lagreVedlegg(11L, 1L, opplastetVedlegg);
        verify(soknadRepository, never()).settDelstegstatus(11L, SKJEMA_VALIDERT);
    }

    @Test
    public void skalIkkeLageDuplikaterAvVedleggPaaEttersending() {
        Faktum faktum = new Faktum().medKey("ekstraVedlegg").medFaktumId(12L).medValue("true");
        Vedlegg ekstraVedlegg = new Vedlegg().medVedleggId(1L).medFaktumId(12L).medSkjemaNummer("N6").medInnsendingsvalg(VedleggKreves);
        List<Vedlegg> vedlegg = new ArrayList<>();
        vedlegg.add(ekstraVedlegg);

        when(soknadDataFletter.hentSoknad("123ABC", true, true)).thenReturn(new WebSoknad().medDelstegStatus(ETTERSENDING_OPPRETTET).medFaktum(faktum).medVedlegg(vedlegg));
        when(vedleggRepository.hentVedlegg("123ABC")).thenReturn(vedlegg);

        List<Vedlegg> paakrevdeVedlegg = vedleggService.genererPaakrevdeVedlegg("123ABC");
        assertEquals(1, paakrevdeVedlegg.size());
        assertEquals(ekstraVedlegg, paakrevdeVedlegg.get(0));
    }

    @Test
    public void skalKunneLagreVedleggMedOppgradertInnsendingsStatus() {
        when(soknadService.hentSoknadFraLokalDb(11L)).thenReturn(new WebSoknad().medDelstegStatus(OPPRETTET));
        Vedlegg vedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(Vedlegg.Status.SendesIkke).medSoknadId(11L);

        vedlegg.setInnsendingsvalg(Vedlegg.Status.SendesSenere);
        vedleggService.lagreVedlegg(1L, vedlegg);
        verify(vedleggRepository).lagreVedlegg(11L, 1L, vedlegg);
    }

    @Test(expected = SendSoknadException.class)
    public void skalIkkeKunneLagreVedleggMedPrioritetMindreEllerLik1() {
        Vedlegg vedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(VedleggKreves);

        vedlegg.setInnsendingsvalg(VedleggKreves);
        vedleggService.lagreVedlegg(1L, vedlegg);
        verify(vedleggRepository, never()).lagreVedlegg(11L, 1L, vedlegg);
    }

    public static byte[] getBytesFromFile(String path) throws IOException {
        InputStream resourceAsStream = VedleggServiceTest.class.getResourceAsStream(path);
        return IOUtils.toByteArray(resourceAsStream);
    }
}
