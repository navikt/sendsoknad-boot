package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import lombok.extern.slf4j.Slf4j;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType.JSON;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class TiltakspengerTilJsonTest {

    private static final String AKTOR_ID = "71";

    private WebSoknad soknad;

    @Before
    public void before() {
        soknad = new WebSoknad();
        List<Faktum> fakta = new ArrayList<>();
        fakta.add(new Faktum()
                .medKey("maalgruppe")
                .medType(Faktum.FaktumType.SYSTEMREGISTRERT)
                .medProperty("kodeverkVerdi", "ARBSOKERE")
                .medProperty("fom", "2015-01-01"));
        fakta.add(new Faktum()
                .medKey("bostotte.aarsak")
                .medValue("fasteboutgifter"));
        fakta.add(new Faktum()
                .medKey("bostotte.periode")
                .medProperty("fom", "2015-07-22")
                .medProperty("tom", "2015-10-22"));
        fakta.add(new Faktum()
                .medKey("bostotte.kommunestotte")
                .medValue("true")
                .medProperty("utgift", "200"));
        fakta.add(new Faktum()
                .medKey("bostotte.adresseutgifter.aktivitetsadresse")
                .medProperty("utgift", "2000"));
        fakta.add(new Faktum()
                .medKey("bostotte.adresseutgifter.hjemstedsaddresse")
                .medProperty("utgift", "3000"));
        fakta.add(new Faktum()
                .medKey("bostotte.adresseutgifter.opphorte")
                .medProperty("utgift", "4000"));
        fakta.add(new Faktum()
                .medKey("bostotte.medisinskearsaker")
                .medValue("true"));

        soknad.setFakta(fakta);
        soknad.medAktorId(AKTOR_ID);
    }

    @Test
    public void test() {
        TiltakspengerTilJson transformer = new TiltakspengerTilJson();
        assertThat(transformer.getRepresentasjonsType()).isEqualTo(JSON);
        AlternativRepresentasjon output = transformer.apply(soknad);
        assertThat(output).isNotNull();
        assertThat(output.getRepresentasjonsType()).isEqualTo(JSON);
    }

}
