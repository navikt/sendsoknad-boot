package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.XmlService;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class XmlServiceTest {

    @Test
    public void fjernerXmlHeadersFraInkludertFil() throws IOException {
        sammenlign("xmlinkluder/fjernxmlheaders/", "root.xml", "fasit.xml");
    }

    @Test
    public void drarUtInnholdOmInkludertFilInneholderSoknadElement() throws IOException {
        sammenlign("xmlinkluder/drautinnhold/", "root.xml", "fasit.xml");
    }

    @Test
    public void inkludererRekursivtMedForskjelligeMapper() throws IOException {
        sammenlign("xmlinkluder/rekursiv/", "root.xml", "fasit.xml");
    }

    private void sammenlign(String filsti, String filnavn, String fasitFil) throws IOException {
        try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(filsti + fasitFil)) {
            assertNotNull(resourceAsStream);

            String xml = XmlService.lastXmlFilMedInclude(filsti, filnavn);
            String fasitXml = IOUtils.toString(resourceAsStream, UTF_8);

            assertEquals(fjernWhitespace(fasitXml), fjernWhitespace(xml));
        }
    }

    private String fjernWhitespace(String xml) {
        return xml.replaceAll(">\\s*<", "><");
    }
}
