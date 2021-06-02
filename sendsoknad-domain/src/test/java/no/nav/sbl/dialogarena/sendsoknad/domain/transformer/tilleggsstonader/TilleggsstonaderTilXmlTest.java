package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Tilleggsstoenadsskjema;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TilleggsstonaderTilXmlTest {

    private final TilleggsstonaderTilXml tilXml = new TilleggsstonaderTilXml(null);
    private WebSoknad soknad;
    private static final String AKTOR_ID = "71";

    @Before
    public void beforeEach() {

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
    public void harMemeTypeApplicationXml() {
        AlternativRepresentasjon alternativRepresentasjon = tilXml.transform(soknad);
        assertEquals("application/xml", alternativRepresentasjon.getMimetype());
    }

    @Test
    public void harFilnavn() {
        AlternativRepresentasjon alternativRepresentasjon = tilXml.transform(soknad);
        assertNotNull(alternativRepresentasjon.getFilnavn());
    }

    @Test
    public void harUuid() {
        AlternativRepresentasjon alternativRepresentasjon = tilXml.transform(soknad);
        assertNotNull(alternativRepresentasjon.getUuid());
    }

    @Test
    public void harContent() {
        AlternativRepresentasjon alternativRepresentasjon = tilXml.transform(soknad);
        assertNotNull(alternativRepresentasjon.getContent());
    }

    @Test
    public void xmlErGyldig() {
        AlternativRepresentasjon alternativRepresentasjon = tilXml.transform(soknad);
        byte[] content = alternativRepresentasjon.getContent();

        try (ByteArrayInputStream stream = new ByteArrayInputStream(content)) {
            Tilleggsstoenadsskjema soknad = JAXB.unmarshal(stream, Tilleggsstoenadsskjema.class);
            assertEquals(AKTOR_ID, soknad.getPersonidentifikator());
        } catch (Exception e) {
            fail("Kunne ikke unmarshalle: " + e.getCause().toString());
        }
    }
}
