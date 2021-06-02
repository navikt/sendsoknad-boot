package no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid;

import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.*;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Virksomhet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArbeidsforholdMock {
    public ArbeidsforholdV3 arbeidMock() {
        ArbeidsforholdV3 amock = mock(ArbeidsforholdV3.class);

        try {
            when(amock.finnArbeidsforholdPrArbeidstaker(any(FinnArbeidsforholdPrArbeidstakerRequest.class))).then((Answer<FinnArbeidsforholdPrArbeidstakerResponse>) invocationOnMock -> {
                FinnArbeidsforholdPrArbeidstakerResponse response = new FinnArbeidsforholdPrArbeidstakerResponse();
                Arbeidsforhold arbeidsforhold = new Arbeidsforhold();
                arbeidsforhold.setAnsettelsesPeriode(hentPeriode());
                arbeidsforhold.getArbeidsavtale().add(lagArbeidsavtale());
                response.getArbeidsforhold().add(arbeidsforhold);
                return response;
            });
        } catch (Exception ignored) {
        }
        return amock;
    }

    private AnsettelsesPeriode hentPeriode() {
        AnsettelsesPeriode periode = new AnsettelsesPeriode();
        Gyldighetsperiode gperiode = new Gyldighetsperiode();
        gperiode.setFom(ServiceUtils.stringTilXmldato("2014-01-02"));
//        gperiode.setTom(ServiceUtils.stringTilXmldato("2015-01-01")); // Denne avgjør om man er ansatt eller ikke for øyeblikket
        periode.setPeriode(gperiode);
        return periode;
    }

    private Arbeidsavtale lagArbeidsavtale() {
        Arbeidsavtale avtale = new Arbeidsavtale();
        avtale.setStillingsprosent(BigDecimal.valueOf(100));
        Avloenningstyper avloenningstyper = new Avloenningstyper();
        avloenningstyper.setKodeRef("fast");
        avtale.setAvloenningstype(avloenningstyper);
        return avtale;
    }


    public OrganisasjonV4 organisasjonMock() {
        OrganisasjonV4 mock = mock(OrganisasjonV4.class);
        try {
            HentOrganisasjonResponse response = new HentOrganisasjonResponse();
            Organisasjon organisasjon = new Virksomhet();
            UstrukturertNavn value = new UstrukturertNavn();
            value.getNavnelinje().add("Mock navn arbeidsgiver");
            organisasjon.setNavn(value);
            response.setOrganisasjon(organisasjon);
            when(mock.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(response);
        } catch (HentOrganisasjonOrganisasjonIkkeFunnet | HentOrganisasjonUgyldigInput hentOrganisasjonOrganisasjonIkkeFunnet) {
            hentOrganisasjonOrganisasjonIkkeFunnet.printStackTrace();
        }
        return mock;
    }
}
