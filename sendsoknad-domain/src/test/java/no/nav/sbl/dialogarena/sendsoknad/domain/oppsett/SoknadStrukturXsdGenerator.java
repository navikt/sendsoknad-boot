package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoknadStrukturXsdGenerator {

    private static final String PATH = "src/main/resources/soknader/";
    private static final String FILENAME = "soknadstruktur.xsd";

    public static final Pattern SOKNAD_SEQUENCE_PATTERN = Pattern.compile("<xs:complexType name=\"soknadStruktur\">(\\s+)<xs:sequence>(.*?)</xs:sequence>", Pattern.DOTALL);

    public static void genererSkjema() throws JAXBException, IOException {
        new SoknadStrukturXsdGenerator().genererOgSkrivTilFil();
    }

    private void genererOgSkrivTilFil() throws JAXBException, IOException {
        String skjemaStreng = lagSkjemaString();
        skjemaStreng = fiksTingDerViErUenigMedJaxbMenFortsattVilHaTingAutogenerert(skjemaStreng);
        skjemaStreng = leggPaFeitAdvarselOmAtFilenErGenerert(skjemaStreng);
        skrivTilFil(skjemaStreng);
    }

    private String leggPaFeitAdvarselOmAtFilenErGenerert(String skjemaStreng) {
        String[] split = skjemaStreng.split("\n", 2);

        String advarsel = "\n" +
                "<!-- ##############################\n" +
                "     #   DENNE FILEN ER GENERERT  #\n" +
                "     #     ALLE ENDRINGER BLIR    #\n" +
                "     #        OVERSKREVET!!       #\n" +
                "     ############################## -->\n";

        return split[0] + advarsel + split[1];
    }

    private String lagSkjemaString() throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance(SoknadStruktur.class);
        final StringWriter writer = new StringWriter();

        jaxbContext.generateSchema(new SchemaOutputResolver() {
            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName) {
                StreamResult result = new StreamResult(writer);
                result.setSystemId(suggestedFileName);
                return result;
            }
        });

        return writer.toString();
    }

    private String fiksTingDerViErUenigMedJaxbMenFortsattVilHaTingAutogenerert(String skjemaStreng) {
        String fiksetSkjema = gjorConfigOptional(skjemaStreng);
        fiksetSkjema = gjorAtSoknadElementerKanLiggeIVilkarligRekkefolge(fiksetSkjema);
        fiksetSkjema = endreLineEndingsFraJaxb(fiksetSkjema);

        return fiksetSkjema;
    }

    private String gjorConfigOptional(String skjemaStreng) {
        return skjemaStreng.replace("<xs:element name=\"configuration\">", "<xs:element name=\"configuration\" minOccurs=\"0\">");
    }

    private String gjorAtSoknadElementerKanLiggeIVilkarligRekkefolge(String skjemaStreng) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = SOKNAD_SEQUENCE_PATTERN.matcher(skjemaStreng);
        matcher.find();

        matcher.appendReplacement(sb, "<xs:complexType name=\"soknadStruktur\">" + matcher.group(1) + "<xs:choice maxOccurs=\"unbounded\">" + matcher.group(2) + "</xs:choice>");
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String endreLineEndingsFraJaxb(String skjemaStreng) {
        return skjemaStreng.replace("\n", System.lineSeparator());
    }

    private void skrivTilFil(String skjemaStreng) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(PATH + FILENAME), StandardCharsets.UTF_8)) {
            writer.write(skjemaStreng);
        }
    }
}
