package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.AktivitetOgMaalgrupperFetcherService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TjenesterRessursTest {

    @InjectMocks
    private TjenesterRessurs ressurs;
    @Mock
    private AktivitetOgMaalgrupperFetcherService aktivitetOgMaalgrupperFetcherService;

    private String fodselsnummer;

    @Before
    public void setUp() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        fodselsnummer = StaticSubjectHandler.getSubjectHandler().getUid();
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
