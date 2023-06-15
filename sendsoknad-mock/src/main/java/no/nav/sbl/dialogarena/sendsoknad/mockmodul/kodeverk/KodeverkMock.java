package no.nav.sbl.dialogarena.sendsoknad.mockmodul.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLPeriode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLTerm;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkResponse;
import org.joda.time.DateMidnight;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KodeverkMock {

    private XMLHentKodeverkResponse postnummerKodeverkResponse() {
        XMLKode kode = new XMLKode()
                .withNavn("0565")
                .withTerm(new XMLTerm().withNavn("Oslo").withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1))))
                .withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));
        return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Kommuner").withKode(kode));
    }

    public KodeverkPortType kodeverkMock() {
        var mock = mock(KodeverkPortType.class);
        try {
            when(mock.hentKodeverk(any())).thenReturn(postnummerKodeverkResponse());
        } catch (HentKodeverkHentKodeverkKodeverkIkkeFunnet e) {
            throw new RuntimeException(e);
        }
        return mock;

    }

}