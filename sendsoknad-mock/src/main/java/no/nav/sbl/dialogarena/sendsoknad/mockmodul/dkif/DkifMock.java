package no.nav.sbl.dialogarena.sendsoknad.mockmodul.dkif;

import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSMobiltelefonnummer;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DkifMock {

    private static final String PERSON_IDENT = "12312312345";
    private static final String EN_EPOST = "test@epost.com";
    private static final String ET_TELEFONNUMMER = "98765432";

    public DigitalKontaktinformasjonV1 dkifMock() {

        DigitalKontaktinformasjonV1 mock = mock(DigitalKontaktinformasjonV1.class);
        WSHentDigitalKontaktinformasjonResponse response = new WSHentDigitalKontaktinformasjonResponse();

        WSKontaktinformasjon kontaktinformasjon = new WSKontaktinformasjon()
                .withPersonident(PERSON_IDENT)
                .withEpostadresse(new WSEpostadresse().withValue(EN_EPOST))
                .withMobiltelefonnummer(new WSMobiltelefonnummer().withValue(ET_TELEFONNUMMER))
                .withReservasjon("TEST");

        response.setDigitalKontaktinformasjon(kontaktinformasjon);

        try {
            when(mock.hentDigitalKontaktinformasjon(any(WSHentDigitalKontaktinformasjonRequest.class))).thenReturn(response);
        } catch (HentDigitalKontaktinformasjonPersonIkkeFunnet | HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet | HentDigitalKontaktinformasjonSikkerhetsbegrensing e) {
            throw new RuntimeException(e);
        }
        return mock;
    }
}
