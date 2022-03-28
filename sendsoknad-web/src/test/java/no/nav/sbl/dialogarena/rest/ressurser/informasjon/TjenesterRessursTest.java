package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import static org.mockito.Mockito.verify;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.AktivitetOgMaalgrupperFetcherService;
import no.nav.sbl.dialogarena.utils.TestTokenUtils;

@RunWith(MockitoJUnitRunner.class)
public class TjenesterRessursTest {

    @InjectMocks
    private TjenesterRessurs ressurs;
    @Mock
    private AktivitetOgMaalgrupperFetcherService aktivitetOgMaalgrupperFetcherService;

    private String fodselsnummer = "01015245464";

    @BeforeClass
    public static void setUp() throws Exception{
        TestTokenUtils.setSecurityContext();
    }

    @Test
    public void skalHenteAktiviteter() {
        ressurs.hentAktiviteter();
        verify(aktivitetOgMaalgrupperFetcherService).hentAktiviteter(fodselsnummer);
    }

    @Test
    public void skalHenteVedtak() {
        ressurs.hentVedtak();
        verify(aktivitetOgMaalgrupperFetcherService).hentVedtak(fodselsnummer);
    }

    @Test
    public void skalHenteMaalgrupper() {
        ressurs.hentMaalgrupper();
        verify(aktivitetOgMaalgrupperFetcherService).hentMaalgrupper(fodselsnummer);
    }
}
