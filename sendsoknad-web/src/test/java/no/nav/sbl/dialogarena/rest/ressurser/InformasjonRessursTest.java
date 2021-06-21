package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.informasjon.InformasjonRessurs;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.InformasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.LandOgPostInfoFetcherService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.PersonInfoFetcherService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Locale;
import java.util.Map;

import static java.util.Collections.singletonList;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InformasjonRessursTest {

    private static final String SOKNADSTYPE = "type";
    private static final String TEMAKODE = "TEMAKODE";

    @Spy
    InformasjonService informasjonService;
    @Spy
    LandOgPostInfoFetcherService landOgPostInfoFetcherService;
    @Mock
    PersonInfoFetcherService personInfoFetcherService;
    @Mock
    PersonaliaBolk personaliaBolk;
    @Mock
    TekstHenter tekstHenter;
    @Mock
    WebSoknadConfig soknadConfig;

    @InjectMocks
    InformasjonRessurs ressurs;

    private Locale norskBokmaal = new Locale("nb", "NO");

    @Before
    public void setup() {
        System.setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        when(personaliaBolk.hentPersonalia(anyString())).thenReturn(personalia());

        SoknadStruktur struktur = new SoknadStruktur();
        struktur.setTemaKode(TEMAKODE);
        struktur.setFakta(singletonList(new FaktumStruktur()));
        when(soknadConfig.hentStruktur(anyString())).thenReturn(struktur);
    }

    @Test
    public void miljovariablerInneholderAlleVariableneViTrenger() {
        Map<String, String> miljovariabler = ressurs.hentMiljovariabler();

        assertThat(miljovariabler.containsKey("saksoversikt.link.url")).isTrue();
        assertThat(miljovariabler.containsKey("dittnav.link.url")).isTrue();
        assertThat(miljovariabler.containsKey("dialogarena.navnolink.url")).isTrue();
        assertThat(miljovariabler.containsKey("soknad.skjemaveileder.url")).isTrue();
        assertThat(miljovariabler.containsKey("soknad.alderspensjon.url")).isTrue();
        assertThat(miljovariabler.containsKey("soknad.reelarbeidsoker.url")).isTrue();
        assertThat(miljovariabler.containsKey("soknad.brukerprofil.url")).isTrue();
        assertThat(miljovariabler.containsKey("dialogarena.cms.url")).isTrue();
        assertThat(miljovariabler.containsKey("soknadinnsending.soknad.path")).isTrue();
        assertThat(miljovariabler.containsKey("soknad.ettersending.antalldager")).isTrue();
    }

    @Test
    public void utslagskriterierInneholderAlleKriteriene() {
        Map<String, Object> utslagskriterier = ressurs.hentUtslagskriterier();
        assertThat(utslagskriterier.containsKey("ytelsesstatus")).isTrue();
        assertThat(utslagskriterier.containsKey("alder")).isTrue();
        assertThat(utslagskriterier.containsKey("fodselsdato")).isTrue();
        assertThat(utslagskriterier.containsKey("bosattINorge")).isTrue();
        assertThat(utslagskriterier.containsKey("registrertAdresse")).isTrue();
        assertThat(utslagskriterier.containsKey("registrertAdresseGyldigFra")).isTrue();
        assertThat(utslagskriterier.containsKey("registrertAdresseGyldigTil")).isTrue();
        assertThat(utslagskriterier.containsKey("erBosattIEOSLand")).isTrue();
        assertThat(utslagskriterier.containsKey("statsborgerskap")).isTrue();

        assertThat(utslagskriterier.size()).isEqualTo(9);
    }

    @Test
    public void spraakDefaulterTilNorskBokmaalHvisIkkeSatt() {
        ressurs.hentTekster(SOKNADSTYPE, null);
        ressurs.hentTekster(SOKNADSTYPE, " ");
        verify(tekstHenter, times(2)).getBundleFor(SOKNADSTYPE, norskBokmaal);
    }

    @Test
    public void skalHenteTeksterForAapViaBundleSoknadaap() {
        ressurs.hentTekster("AAP", null);
        verify(tekstHenter).getBundleFor("soknadaap", norskBokmaal);
    }

    @Test
    public void skalHenteTeksterForAlleBundlesUtenType() {
        ressurs.hentTekster("", null);
        verify(tekstHenter).getBundleFor("", norskBokmaal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void kastExceptionHvisIkkeSpraakErPaaRiktigFormat() {
        ressurs.hentTekster(SOKNADSTYPE, "NORSK");
    }

    @Test
    public void returnerFullStrukturHvisIkkeFilterErSatt() {
        SoknadStruktur struktur = ressurs.hentSoknadStruktur("NAV123", null);
        assertThat(struktur.getTemaKode()).isEqualTo(TEMAKODE);
        assertThat(struktur.getFakta()).isNotEmpty();
    }

    @Test
    public void returnerStrukturMedBareTemakodeHvisFilterErSattTilTemakode() {
        SoknadStruktur struktur = ressurs.hentSoknadStruktur("NAV123", "temakode");
        assertThat(struktur.getTemaKode()).isEqualTo(TEMAKODE);
        assertThat(struktur.getFakta()).isEmpty();
    }

    private Personalia personalia() {
        Personalia personalia = new Personalia();
        personalia.setFnr("12312312345");
        Adresse adresse = new Adresse();
        personalia.setGjeldendeAdresse(adresse);
        return personalia;
    }
}
