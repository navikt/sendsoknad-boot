package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UgyldigOpplastingTypeException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import wiremock.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.nav.sbl.dialogarena.rest.ressurser.VedleggRessurs.MAKS_TOTAL_FILSTORRELSE;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VedleggRessursTest {

    private static final String NORMAL_PDF = "pdfs/navskjema.pdf";
    private static final String MINIMAL_PDF = "pdfs/minimal.pdf";
    private static final String ENCRYPTED_PDF = "pdfs/endringsbeskyttet.pdf";
    private static final String SIGNED_PDF = "pdfs/signed.pdf";
    private static final String IMAGE = "img/hake_sort_200.jpg";
    private static final String ILLEGAL_FILETYPE = "img/nav-logo-stor.gif";

    private static final long VEDLEGGSID = 1;
    private static final String BEHANDLINGSID = "123";
    @Mock
    private SoknadService soknadService;
    @Mock
    private VedleggService vedleggService;
    @InjectMocks
    private VedleggRessurs ressurs;

    @Before
    public void setup() {
        WebSoknad soknad = new WebSoknad().medFortsettSoknadUrl("url").medId(71);
        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(soknad);
        when(vedleggService.hentVedlegg(VEDLEGGSID, false)).thenReturn(new Vedlegg());
    }

    @Test(expected = OpplastingException.class)
    public void uploadShouldThrowExceptionIfTheAttatchmentsAreTooLarge() {
        Vedlegg vedlegg = createVedlegg(MAKS_TOTAL_FILSTORRELSE + 1L);
        when(vedleggService.hentVedleggUnderBehandling(eq(BEHANDLINGSID), anyString())).thenReturn(singletonList(vedlegg));

        ressurs.lastOppFiler(VEDLEGGSID, BEHANDLINGSID, Collections.emptyList());
        verify(vedleggService, never()).lagreVedlegg(any(Vedlegg.class));
    }

    @Test
    public void uploadFiles_noFile_shouldWorkFineButSaveNothing() {
        Vedlegg vedlegg = createVedlegg();
        when(vedleggService.hentVedleggUnderBehandling(eq(BEHANDLINGSID), anyString())).thenReturn(singletonList(vedlegg));

        ressurs.lastOppFiler(VEDLEGGSID, BEHANDLINGSID, Collections.emptyList());

        verify(vedleggService, never()).lagreVedlegg(any(Vedlegg.class));
    }

    @Test
    public void uploadFiles_twoProperPdfs_shouldWorkFine() throws URISyntaxException, IOException {
        long newlyCreatedVedleggsId0 = 71L;
        long newlyCreatedVedleggsId1 = 68L;
        long newlyCreatedVedleggsSize0 = 63L;
        long newlyCreatedVedleggsSize1 = 65L;

        Vedlegg vedlegg = createVedlegg();
        when(vedleggService.lagreVedlegg(any(Vedlegg.class))).thenReturn(newlyCreatedVedleggsId0, newlyCreatedVedleggsId1);
        when(vedleggService.hentVedlegg(newlyCreatedVedleggsId0, false)).thenReturn(createVedlegg(newlyCreatedVedleggsSize0));
        when(vedleggService.hentVedlegg(newlyCreatedVedleggsId1, false)).thenReturn(createVedlegg(newlyCreatedVedleggsSize1));

        List<Vedlegg> result = ressurs.uploadFiles(BEHANDLINGSID, vedlegg, asList(getTestFile(MINIMAL_PDF), getTestFile(NORMAL_PDF)));

        assertEquals(2, result.size());
        assertEquals(newlyCreatedVedleggsSize0, (long) result.get(0).getStorrelse());
        assertEquals(newlyCreatedVedleggsSize1, (long) result.get(1).getStorrelse());
        verify(vedleggService, times(2)).lagreVedlegg(any(Vedlegg.class));
    }

    @Test
    public void uploadFiles_onePdfAndOneImage_shouldWorkFine() throws URISyntaxException, IOException {
        long newlyCreatedVedleggsId0 = 71L;
        long newlyCreatedVedleggsId1 = 68L;
        long newlyCreatedVedleggsSize0 = 63L;
        long newlyCreatedVedleggsSize1 = 65L;

        Vedlegg vedlegg = createVedlegg();
        when(vedleggService.lagreVedlegg(any(Vedlegg.class))).thenReturn(newlyCreatedVedleggsId0, newlyCreatedVedleggsId1);
        when(vedleggService.hentVedlegg(newlyCreatedVedleggsId0, false)).thenReturn(createVedlegg(newlyCreatedVedleggsSize0));
        when(vedleggService.hentVedlegg(newlyCreatedVedleggsId1, false)).thenReturn(createVedlegg(newlyCreatedVedleggsSize1));

        List<Vedlegg> result = ressurs.uploadFiles(BEHANDLINGSID, vedlegg, asList(getTestFile(MINIMAL_PDF), getTestFile(IMAGE)));

        assertEquals(2, result.size());
        assertEquals(newlyCreatedVedleggsSize0, (long) result.get(0).getStorrelse());
        assertEquals(newlyCreatedVedleggsSize1, (long) result.get(1).getStorrelse());
        verify(vedleggService, times(2)).lagreVedlegg(any(Vedlegg.class));
    }

    @Test
    public void uploadFiles_illegalFiletype_shouldThrowException() throws URISyntaxException, IOException {
        Vedlegg vedlegg = createVedlegg();

        try {
            ressurs.uploadFiles(BEHANDLINGSID, vedlegg, singletonList(getTestFile(ILLEGAL_FILETYPE)));
            fail("Expected exception to be thrown");
        } catch (UgyldigOpplastingTypeException e) {
            assertEquals("Ugyldig filtype for opplasting", e.getMessage());
        }

        verify(vedleggService, never()).lagreVedlegg(any(Vedlegg.class));
    }

    @Test
    public void uploadFiles_encryptedFile_shouldThrowException() throws URISyntaxException, IOException {
        Vedlegg vedlegg = createVedlegg();

        try {
            ressurs.uploadFiles(BEHANDLINGSID, vedlegg, singletonList(getTestFile(ENCRYPTED_PDF)));
            fail("Expected exception to be thrown");
        } catch (UgyldigOpplastingTypeException e) {
            assertEquals("Klarte ikke å sjekke om vedlegget er gyldig", e.getMessage());
        }

        verify(vedleggService, never()).lagreVedlegg(any(Vedlegg.class));
    }

    @Test
    public void uploadFiles_signedPdf_shouldWorkFine() throws URISyntaxException, IOException {
        long newlyCreatedVedleggsId = 71L;
        long newlyCreatedVedleggsSize = 63L;

        Vedlegg vedlegg = createVedlegg();
        when(vedleggService.lagreVedlegg(any(Vedlegg.class))).thenReturn(newlyCreatedVedleggsId);
        when(vedleggService.hentVedlegg(newlyCreatedVedleggsId, false)).thenReturn(createVedlegg(newlyCreatedVedleggsSize));

        List<Vedlegg> result = ressurs.uploadFiles(BEHANDLINGSID, vedlegg, singletonList(getTestFile(SIGNED_PDF)));

        assertEquals(1, result.size());
        assertEquals(newlyCreatedVedleggsSize, (long) result.get(0).getStorrelse());
        verify(vedleggService, times(1)).lagreVedlegg(any(Vedlegg.class));
    }

    private static Vedlegg createVedlegg() {
        return createVedlegg(71L);
    }

    private static Vedlegg createVedlegg(long size) {
        Vedlegg vedlegg = new Vedlegg();
        vedlegg.setStorrelse(size);
        vedlegg.setNavn("Test");
        vedlegg.setData("".getBytes());
        vedlegg.setSkjemaNummer("NAV 71-68.78");
        return vedlegg;
    }

    private static byte[] getTestFile(String filename) throws IOException, URISyntaxException {
        URL url = ClassLoader.getSystemResource(filename);
        return FileUtils.readFileToByteArray(new File(url.toURI()));
    }
}
