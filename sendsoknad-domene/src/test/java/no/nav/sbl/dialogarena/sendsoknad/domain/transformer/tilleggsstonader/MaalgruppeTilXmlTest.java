package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Maalgruppeinformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.datatype.XMLGregorianCalendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class MaalgruppeTilXmlTest {

    private Faktum maalgruppeFaktum;

    @Before
    public void beforeEach() {
        maalgruppeFaktum = new Faktum().medType(Faktum.FaktumType.SYSTEMREGISTRERT)
                .medProperty("kodeverkVerdi", "ARBSOKERE")
                .medProperty("fom", "2015-01-01")
                .medProperty("tom", "2015-02-02");
    }

    @Test
    public void setterKodeverkRefFraFaktum() {
        Maalgruppeinformasjon maalgruppeinformasjon = MaalgruppeTilXml.transform(maalgruppeFaktum);

        assertNotNull(maalgruppeinformasjon);
        assertThat(maalgruppeinformasjon.getMaalgruppetype().getKodeverksRef()).isEqualTo("ARBSOKERE");
    }

    @Test
    public void setterPeriodeFraFaktum() {
        Maalgruppeinformasjon maalgruppeinformasjon = MaalgruppeTilXml.transform(maalgruppeFaktum);

        assertNotNull(maalgruppeinformasjon);
        XMLGregorianCalendar fom = maalgruppeinformasjon.getPeriode().getFom();
        assertThat(fom.getDay()).isEqualTo(1);
        assertThat(fom.getMonth()).isEqualTo(1);
        assertThat(fom.getYear()).isEqualTo(2015);

        XMLGregorianCalendar tom = maalgruppeinformasjon.getPeriode().getTom();
        assertThat(tom.getDay()).isEqualTo(2);
        assertThat(tom.getMonth()).isEqualTo(2);
        assertThat(tom.getYear()).isEqualTo(2015);
    }

    @Test
    public void tilDatoErIkkeObligatorisk() {
        Maalgruppeinformasjon maalgruppeinformasjon = MaalgruppeTilXml.transform(maalgruppeFaktum.medProperty("tom", null));

        assertNotNull(maalgruppeinformasjon);
        XMLGregorianCalendar tom = maalgruppeinformasjon.getPeriode().getTom();
        assertThat(tom).isNull();
    }

    @Test
    public void tomPeriode() {
        Maalgruppeinformasjon maalgruppeinformasjon = MaalgruppeTilXml.transform(maalgruppeFaktum.medProperty("fom", null).medProperty("tom", null));

        assertNotNull(maalgruppeinformasjon);
        assertThat(maalgruppeinformasjon.getPeriode()).isNull();
    }
}
