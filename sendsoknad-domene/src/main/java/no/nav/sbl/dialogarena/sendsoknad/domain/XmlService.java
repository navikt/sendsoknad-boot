package no.nav.sbl.dialogarena.sendsoknad.domain;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class XmlService {

    public static final Pattern INCLUDE_PATTERN = Pattern.compile("<xi:include href=\"(.*?)\" .*?>");
    public static final Pattern SOKNAD_PATTERN = Pattern.compile("<soknad .*?>(.*)</soknad>", Pattern.DOTALL);
    public static final String XML_HEADER_PATTERN = "<\\?xml .*? ?>";

    public static StreamSource lastXmlFil(String fil) throws IOException {
        String filsti = FilenameUtils.getFullPath(fil);
        String filNavn = FilenameUtils.getName(fil);

        return new StreamSource(new StringReader(lastXmlFilMedInclude(filsti, filNavn)));
    }

    public static String lastXmlFilMedInclude(String filsti, String filNavn) throws IOException {
        String mainFile = hentFilInnhold(filsti + filNavn);

        Matcher matcher = INCLUDE_PATTERN.matcher(mainFile);
        StringBuffer fullXml = new StringBuffer();

        while (matcher.find()) {
            String includeHref = matcher.group(1);
            String includeXml = hentInkludertXml(filsti, includeHref);

            matcher.appendReplacement(fullXml, Matcher.quoteReplacement(includeXml));
        }
        matcher.appendTail(fullXml);

        return fullXml.toString();
    }

    private static String hentInkludertXml(String filsti, String href) throws IOException {
        String includePath = filsti + FilenameUtils.getFullPath(href);
        String includeFile = FilenameUtils.getName(href);

        String includeXml = lastXmlFilMedInclude(includePath, includeFile);
        includeXml = fiksInkludertXml(includeXml);
        return includeXml;
    }

    private static String hentFilInnhold(String fil) throws IOException {
        fil = FilenameUtils.normalize(fil);
        fil = FilenameUtils.separatorsToUnix(fil);
        try (InputStream stream = XmlService.class.getClassLoader().getResourceAsStream(fil)) {
            assert stream != null;
            return IOUtils.toString(stream, UTF_8);
        }
    }

    private static String fiksInkludertXml(String innhold) {
        Matcher childMatcher = SOKNAD_PATTERN.matcher(innhold);
        if (childMatcher.find()) {
            return childMatcher.group(1);
        } else {
            return innhold.replaceAll(XML_HEADER_PATTERN, "");
        }
    }
}
