package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Barn;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.TilsynsutgifterBarn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.math.BigInteger.ZERO;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.StofoKodeverkVerdier.BarnepassAarsak.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TilsynBarnepassTilXmlTest {

    @Mock
    private TekstHenter tekstHenter;

    long barnId = 10;
    private TilsynsutgifterBarn tilsynsutgifterBarnXml;
    private WebSoknad soknad;

    @Before
    public void beforeEach() {
        soknad = new WebSoknad();
        List<Faktum> fakta = new ArrayList<>();

        fakta.add(new Faktum().medKey("barnepass.periode").medProperty("fom", "2015-01-01").medProperty("tom", "2016-01-01"));

        soknad.setFakta(fakta);

        when(tekstHenter.finnTekst(eq(trengertilsyn.cmsKey), isNull(), any(Locale.class))).thenReturn("tilsyn");
        when(tekstHenter.finnTekst(eq(langvarig.cmsKey), isNull(), any(Locale.class))).thenReturn("langvarig");
        when(tekstHenter.finnTekst(eq(ingen.cmsKey), isNull(), any(Locale.class))).thenReturn("ingen");
    }

    @Test
    public void skalLeggeTilPeriode() {
        tilsynsutgifterBarnXml = TilsynBarnepassTilXml.transform(soknad, tekstHenter);
        assertThat(tilsynsutgifterBarnXml.getPeriode().getFom()).is(StofoTestUtils.periodeMatcher(2015, 1, 1));
        assertThat(tilsynsutgifterBarnXml.getPeriode().getTom()).is(StofoTestUtils.periodeMatcher(2016, 1, 1));
    }

    @Test
    public void skalLeggeTilBarnSomDetSokesBarnepassFor() {
        String oleNavn = "Ole Mockmann";
        String oleFnr = "11111111111";

        String doleNavn = "Dole Mockmann";
        String doleFodselsdag = "1991-01-08";

        String doffenNavn = "Doffen Mockmann";
        String doffenFnr = "12312312312";

        String annenForsorger = "22222222222";

        String barnehage = "barnehage";
        String dagmamma = "dagmamma";
        String privat = "privat";

        leggTilBarn("fnr", oleFnr, oleNavn, "true", ZERO, annenForsorger, barnehage,  true, true, false, false, SYSTEMREGISTRERT);
        leggTilBarn("fodselsdato", doleFodselsdag, doleNavn, "true", ZERO, annenForsorger, dagmamma, false, false, true, false, BRUKERREGISTRERT);
        leggTilBarn("fnr", doffenFnr, doffenNavn, "false", ZERO, null, privat, false, false, false, false, SYSTEMREGISTRERT);

        tilsynsutgifterBarnXml = TilsynBarnepassTilXml.transform(soknad, tekstHenter);
        List<Barn> barn = tilsynsutgifterBarnXml.getBarn();
        assertThat(barn.size()).isEqualTo(2);


        assertThat(barn.get(0).getNavn()).isEqualTo("Ole");
        assertThat(barn.get(0).getPersonidentifikator()).isEqualTo(oleFnr);
        assertThat(barn.get(0).getTilsynskategori().getValue()).isEqualTo(StofoKodeverkVerdier.TilsynForetasAvKodeverk.barnehage.kodeverksverdi);
        assertThat(barn.get(0).isHarFullfoertFjerdeSkoleaar()).isEqualTo(true);
        assertEquals(barn.get(0).getMaanedligUtgiftTilsynBarn(), ZERO);
        assertThat(barn.get(0).getAarsakTilBarnepass()).contains("tilsyn");

        assertThat(barn.get(1).getNavn()).isEqualTo("Dole");
        assertThat(barn.get(1).getPersonidentifikator()).isEqualTo(doleFodselsdag);
        assertThat(barn.get(1).getTilsynskategori().getValue()).isEqualTo(StofoKodeverkVerdier.TilsynForetasAvKodeverk.dagmamma.kodeverksverdi);
        assertThat(barn.get(1).isHarFullfoertFjerdeSkoleaar()).isEqualTo(false);
        assertEquals(barn.get(1).getMaanedligUtgiftTilsynBarn(), ZERO);
        assertThat(tilsynsutgifterBarnXml.getAnnenForsoergerperson()).isEqualTo(annenForsorger);
        assertThat(barn.get(1).getAarsakTilBarnepass()).contains("langvarig");
    }

    @Test
    public void skalHandtereNyttFaktumForBarnepassFjerdeklasseAarsak() {
        String oleNavn = "Ole Mockmann";
        String oleFnr = "11111111111";
        String annenForsorger = "22222222222";

        String barnehage = "barnehage";
        String aarsak = "trengertilsyn";
        leggTilBarnUtenAarsakFaktum("fnr", oleFnr, oleNavn, "true", annenForsorger, barnehage, true, SYSTEMREGISTRERT);
        long faktumId = soknad.getFaktumMedKey("barn").getFaktumId();
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 10000).medKey(TilsynBarnepassTilXml.BARNEPASS_AARSAK).medValue(aarsak));

        tilsynsutgifterBarnXml = TilsynBarnepassTilXml.transform(soknad, tekstHenter);
        List<Barn> barn = tilsynsutgifterBarnXml.getBarn();
        assertThat(barn.get(0).getAarsakTilBarnepass()).contains("tilsyn");
    }

    @Test
    public void skalHandtereNyttOgGammeltFaktumSamtidigForBarnepassFjerdeklasseAarsak() {
        String oleNavn = "Ole Mockmann";
        String oleFnr = "11111111111";
        String annenForsorger = "22222222222";

        String barnehage = "barnehage";
        leggTilBarn("fnr", oleFnr, oleNavn, "true", ZERO, annenForsorger, barnehage, true, true, false, false, SYSTEMREGISTRERT);

        long faktumId = soknad.getFaktumMedKey("barn").getFaktumId();
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 10000).medKey(TilsynBarnepassTilXml.BARNEPASS_AARSAK).medValue("ingen"));

        tilsynsutgifterBarnXml = TilsynBarnepassTilXml.transform(soknad, tekstHenter);
        List<Barn> barn = tilsynsutgifterBarnXml.getBarn();
        assertThat(barn.get(0).getAarsakTilBarnepass()).contains("ingen");
        assertThat(barn.get(0).getAarsakTilBarnepass().size()).isEqualTo(1);
    }

    @Test
    public void skalHandtereIkkeFullfoertFjerdeklasse() {
        String oleNavn = "Ole Mockmann";
        String oleFnr = "11111111111";
        String annenForsorger = "22222222222";

        String barnehage = "barnehage";
        leggTilBarnUtenAarsakFaktum("fnr", oleFnr, oleNavn, "true", annenForsorger, barnehage, false, SYSTEMREGISTRERT);

        tilsynsutgifterBarnXml = TilsynBarnepassTilXml.transform(soknad, tekstHenter);
        List<Barn> barn = tilsynsutgifterBarnXml.getBarn();
        assertThat(barn.get(0).getAarsakTilBarnepass().isEmpty()).isTrue();
    }

    public void leggTilBarnUtenAarsakFaktum(String identifikatorType, String identifikator, String navn, String sokesOm,
                                            String annenForsorger, String type, boolean fullortFjerdeSkolear,
                                            Faktum.FaktumType barnefaktumType) {

        long faktumId = barnId++;
        soknad.getFakta().add(new Faktum().medKey("barn")
                .medFaktumId(faktumId)
                .medProperty(identifikatorType, identifikator)
                .medProperty("sammensattnavn", navn)
                .medProperty("fornavn", navn.split(" ")[0])
                .medProperty("etternavn", navn.split(" ")[1])
                .medType(barnefaktumType));
        soknad.getFakta().add(new Faktum().medKey("andreforelder").medValue(annenForsorger));
        soknad.getFakta().add(new Faktum()
                .medFaktumId(faktumId + 1000)
                .medKey("barnepass.sokerbarnepass")
                .medValue(sokesOm)
                .medProperty("tilknyttetbarn", "" + faktumId)
                .medProperty("sokerOmBarnepass", sokesOm));

        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_TYPER).medValue(type));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_ANDREFORELDER).medValue(annenForsorger));
        soknad.getFakta().add(new Faktum().medFaktumId(faktumId + 10000).medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_FOLLFORT_FJERDE).medValue("" + fullortFjerdeSkolear));
    }

    private void leggTilBarn(String identifikatorType, String identifikator, String navn, String sokesOm, BigInteger sokeutgift,
                             String annenForsorger, String type, boolean fullortFjerdeSkolear, boolean tilsyn, boolean langvarig,
                             boolean ingen, Faktum.FaktumType barnefaktumType) {

        long faktumId = barnId++;
        soknad.getFakta().add(new Faktum().medKey("barn")
                .medFaktumId(faktumId)
                .medProperty(identifikatorType, identifikator)
                .medProperty("sammensattnavn", navn)
                .medProperty("fornavn", navn.split(" ")[0])
                .medProperty("etternavn", navn.split(" ")[1])
                .medType(barnefaktumType));
        soknad.getFakta().add(new Faktum().medKey("andreforelder").medValue(annenForsorger));
        soknad.getFakta().add(new Faktum()
                .medFaktumId(faktumId + 1000)
                .medKey("barnepass.sokerbarnepass")
                .medValue(sokesOm)
                .medProperty(sokesOm+"utgift", sokeutgift.toString())
                .medProperty("tilknyttetbarn", "" + faktumId)
                .medProperty("sokerOmBarnepass", sokesOm));

        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_TYPER).medValue(type));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_ANDREFORELDER).medValue(annenForsorger));
        soknad.getFakta().add(new Faktum().medFaktumId(faktumId + 10000).medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_FOLLFORT_FJERDE).medValue("" + fullortFjerdeSkolear));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 10000).medKey(TilsynBarnepassTilXml.BARNEPASS_AARSAKER.get(0)).medValue("" + langvarig));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 10000).medKey(TilsynBarnepassTilXml.BARNEPASS_AARSAKER.get(1)).medValue("" + tilsyn));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 10000).medKey(TilsynBarnepassTilXml.BARNEPASS_AARSAKER.get(2)).medValue("" + ingen));
    }
}
