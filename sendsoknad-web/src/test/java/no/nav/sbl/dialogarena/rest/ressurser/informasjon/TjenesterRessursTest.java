package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.AktivitetOgMaalgrupperFetcherService;
import no.nav.sbl.dialogarena.utils.TestTokenUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TjenesterRessursTest {

    @InjectMocks
    private TjenesterRessurs ressurs;
    @Mock
    private AktivitetOgMaalgrupperFetcherService aktivitetOgMaalgrupperFetcherService;

    private final String fodselsnummer = "10108000398"; // Ikke ekte person

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
