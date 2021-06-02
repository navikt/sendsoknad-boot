package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;

import no.nav.sbl.dialogarena.sendsoknad.domain.XmlService;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class SoknadXmlValidererTest {

    @BeforeClass
    public static void genererXsd() throws JAXBException, IOException {
        SoknadStrukturXsdGenerator.genererSkjema();
    }

    @Test
    public void testTilleggStonaderXml() throws Exception {
        testOmXmlValiderer("soknadtilleggsstonader.xml");
    }

    @Test
    public void testRefusjonXml() throws Exception {
        testOmXmlValiderer("refusjondagligreise.xml");
    }

    @Test
    public void testTiltakspengerXml() throws Exception {
        testOmXmlValiderer("tiltakspenger.xml");
    }

    @Test
    public void testBilstonadXml() throws Exception {
        testOmXmlValiderer("bilstonad.xml");
    }

    @Test
    public void testAapUtland() throws Exception {
        testOmXmlValiderer("aap_utland.xml");
    }

    @Test
    public void testAapOrdinaerXml() throws Exception {
        testOmXmlValiderer("aap/aap_ordinaer.xml");
    }

    @Test
    public void testAapGjenopptakXml() throws Exception {
        testOmXmlValiderer("aap/aap_gjenopptak.xml");
    }

    private void testOmXmlValiderer(String xmlFilNavn) throws Exception {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        File xsdFil = Paths.get("src/main/resources/soknader/soknadstruktur.xsd").toFile();
        Schema schema = schemaFactory.newSchema(xsdFil);
        Validator validator = schema.newValidator();

        StreamSource xmlSource = XmlService.lastXmlFil("soknader/" + xmlFilNavn);

        validator.validate(xmlSource);
    }
}
