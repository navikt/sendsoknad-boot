package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.*;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArbeidsforholdTransformerTest {

    private static final String ORGNUMMER = "1234567";
    @Mock
    private OrganisasjonV4 organisasjon;

    @InjectMocks
    private ArbeidsforholdTransformer arbeidsforholdTransformer;

    private DatatypeFactory datatypeFactory;

    @Before
    public void setup() throws Exception {
        datatypeFactory = DatatypeFactory.newInstance();
        when(organisasjon.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(createOrgResponse());
    }

    private HentOrganisasjonResponse createOrgResponse() {
        HentOrganisasjonResponse response = new HentOrganisasjonResponse();
        no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon org = new no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon();
        UstrukturertNavn value = new UstrukturertNavn();
        value.getNavnelinje().add("Testesen A/S");
        value.getNavnelinje().add("andre linje");
        org.setNavn(value);
        response.setOrganisasjon(org);
        return response;
    }

    @Test
    public void skalTransformereFastArbeidsforhold() {
        Arbeidsforhold arbeidsforhold = arbeidsforholdTransformer.transform(lagArbeidsforhold("fast"));
        assertTrue(arbeidsforhold.harFastStilling);
        assertEquals(Long.valueOf(100L), arbeidsforhold.fastStillingsprosent);
        assertFalse(arbeidsforhold.variabelStillingsprosent);
        assertEquals("2015-01-01", arbeidsforhold.fom);
        assertNull(arbeidsforhold.tom);
        assertEquals("Testesen A/S, andre linje", arbeidsforhold.arbeidsgivernavn);
    }

    @Ignore
    @Test
    public void skalTransformereVariabeltArbeidsforhold() {
        Arbeidsforhold result = arbeidsforholdTransformer.transform(lagArbeidsforhold("time"));
        assertFalse(result.harFastStilling);
        assertEquals(Long.valueOf(0L), result.fastStillingsprosent);
        assertTrue(result.variabelStillingsprosent);
    }

    @Ignore
    @Test
    public void skalTransformereMixedArbeidsforhold() {
        Arbeidsforhold result = arbeidsforholdTransformer.transform(lagArbeidsforhold("time", "fast"));
        assertTrue(result.harFastStilling);
        assertEquals(Long.valueOf(100L), result.fastStillingsprosent);
        assertTrue(result.variabelStillingsprosent);
    }

    private no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold lagArbeidsforhold(String... stillingstyper) {
        no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold = new no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold();
        arbeidsforhold.setArbeidsgiver(lagOrganisasjon());
        arbeidsforhold.setAnsettelsesPeriode(lagAapenPeriode(new DateTime(2015, 1, 1, 0, 0)));
        arbeidsforhold.setArbeidsforholdIDnav(12345L);
        for (String stillingstype : stillingstyper) {
            arbeidsforhold.getArbeidsavtale().add(lagArbeidsavtale(stillingstype));
        }
        return arbeidsforhold;
    }

    private Arbeidsavtale lagArbeidsavtale(String stillingstype) {
        Arbeidsavtale arbeidsavtale = new Arbeidsavtale();
        Avloenningstyper value = new Avloenningstyper();
        value.setKodeRef(stillingstype);
        arbeidsavtale.setAvloenningstype(value);
        if ("fast".equals(stillingstype)) {
            arbeidsavtale.setStillingsprosent(new BigDecimal("100"));
        }
        return arbeidsavtale;
    }

    private AnsettelsesPeriode lagAapenPeriode(DateTime dateTime) {
        AnsettelsesPeriode ansettelsesPeriode = new AnsettelsesPeriode();
        ansettelsesPeriode.setPeriode(lagPeriode(dateTime, null));
        return ansettelsesPeriode;
    }

    private Gyldighetsperiode lagPeriode(DateTime fom, DateTime tom) {
        Gyldighetsperiode gyldighetsperiode = new Gyldighetsperiode();
        gyldighetsperiode.setFom(datatypeFactory.newXMLGregorianCalendar(fom.toGregorianCalendar()));
        if (tom != null) {
            gyldighetsperiode.setTom(datatypeFactory.newXMLGregorianCalendar(tom.toGregorianCalendar()));
        }
        return gyldighetsperiode;
    }

    private Aktoer lagOrganisasjon() {
        Organisasjon org = new Organisasjon();
        org.setOrgnummer(ORGNUMMER);
        return org;
    }
}
