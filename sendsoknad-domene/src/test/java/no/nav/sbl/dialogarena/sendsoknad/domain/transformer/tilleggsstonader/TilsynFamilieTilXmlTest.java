package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.TilsynsutgifterFamilie;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TilsynFamilieTilXmlTest {

    private WebSoknad websoknad;

    @Before
    public void beforeEach() {
        websoknad = new WebSoknad();
        List<Faktum> fakta = new ArrayList<>();
        fakta.add(new Faktum(). medKey("personalia").medProperty("fno", "11111122222"));
        fakta.add(new Faktum()
                .medKey("tilsynfamilie.periode")
                .medProperty("fom", "2015-07-22")
                .medProperty("tom", "2015-10-22"));
        fakta.add(new Faktum()
                .medKey("tilsynfamilie.persontilsyn")
                .medValue("12312312345"));
        fakta.add(new Faktum()
                        .medKey("tilsynfamilie.typetilsyn")
                        .medProperty("offentlig", "true")
                        .medProperty("privat", "true")
                        .medProperty("annet", "true")
        );
        fakta.add(new Faktum()
                .medKey("tilsynfamilie.deletilsyn")
                .medValue("true")
                .medProperty("personnummer", "12332112345")
                .medProperty("nav", "test testesen"));

        websoknad.setFakta(fakta);
    }

    @Test
    public void settHarFasteBoutgifter() {
        TilsynsutgifterFamilie transform = TilsynFamilieTilXml.transform(websoknad);
        assertThat(transform.getPeriode().getFom()).is(StofoTestUtils.periodeMatcher(2015, 7, 22));
        assertThat(transform.getPeriode().getTom()).is(StofoTestUtils.periodeMatcher(2015, 10, 22));

        assertThat(transform.getTilsynsmottaker()).isEqualToIgnoringCase("12312312345");
        assertThat(transform.getAnnenTilsynsperson()).isEqualTo("12332112345");
        assertThat(transform.getTilsynForetasAv()).contains("Offentlig", "Privat", "Annet");
    }

    @Test
    public void skalIkkeSetteAnnenTIlsynspersonOmValueErFalse(){
        websoknad.getFaktumMedKey("tilsynfamilie.deletilsyn").setValue("false");
        TilsynsutgifterFamilie transform = TilsynFamilieTilXml.transform(websoknad);
        assertThat(transform.getAnnenTilsynsperson()).isNull();
    }
}
