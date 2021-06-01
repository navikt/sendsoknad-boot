package no.nav.sbl.pdfutility;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;
import org.slf4j.Logger;

import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.slf4j.LoggerFactory.getLogger;

class KonverterTilPdf {

    private static final Logger LOGGER = getLogger(KonverterTilPdf.class);

    static byte[] createPDFFromImage(byte[] image) {
        try (PDDocument doc = new PDDocument()) {

            PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, image, null);
            Dimension scaledSize = getScaledDimension(pdImage.getWidth(), pdImage.getHeight());
            PDPage page = new PDPage(new PDRectangle(scaledSize.width, scaledSize.height));
            doc.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true)) {
                contentStream.drawImage(pdImage, 0, 0, scaledSize.width, scaledSize.height);
            }
            addFonts(doc);
            addDC(doc);
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                doc.save(byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            }
        } catch (IOException ioe) {
            LOGGER.error("Klarte ikke å sjekke filtype til PDF. Feil: '{}'", ioe.getMessage());
            throw new RuntimeException("vedlegg.opplasting.feil.generell");
        }
    }

    private static void addFonts(PDDocument doc) {
        try (InputStream fontis = KonverterTilPdf.class.getResourceAsStream("/fonts/arial/ArialMT.ttf")) {
            PDFont font = PDType0Font.load(doc, fontis);
            if (!font.isEmbedded()) {
                LOGGER.warn("Klarte ikke å laste default påkrevde fonter ved konvertering til PDF/A");
            }
        } catch (IOException e) {
            LOGGER.error("Lasting av fonter ved konvertering til PDF/A feilet {}", e.getMessage());
        }
    }

    private static void addDC(PDDocument doc) {
        XMPMetadata xmp = XMPMetadata.createXMPMetadata();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            DublinCoreSchema dc = xmp.createAndAddDublinCoreSchema();
            dc.setTitle("image");

            PDFAIdentificationSchema id = xmp.createAndAddPFAIdentificationSchema();
            id.setPart(1);
            id.setConformance("B");

            XmpSerializer serializer = new XmpSerializer();

            serializer.serialize(xmp, baos, true);

            PDMetadata metadata = new PDMetadata(doc);
            metadata.importXMPMetadata(baos.toByteArray());
            doc.getDocumentCatalog().setMetadata(metadata);
        } catch (BadFieldValueException e) {
            // won't happen here, as the provided value is valid
            throw new IllegalArgumentException(e);
        } catch (IOException | TransformerException ioe) {
            LOGGER.error("Feil ved lasting av XMPMetadata ved konvertering av Image til PDF/A, {}", ioe.getMessage());
        }

        try (InputStream colorProfile = KonverterTilPdf.class.getResourceAsStream("/icc/AdobeRGB1998.icc")) {
            // sRGB output intent
            PDOutputIntent intent = new PDOutputIntent(doc, colorProfile);
            intent.setInfo("AdobeRGB1998");
            intent.setOutputCondition("sRGB IEC61966-2.1");
            intent.setOutputConditionIdentifier("sRGB IEC61966-2.1");
            intent.setRegistryName("http://www.color.org");
            doc.getDocumentCatalog().addOutputIntent(intent);
        } catch (IOException e) {
            LOGGER.error("Feil ved lasting av XMPMetadata ved konvertering av Image til PDF/A, {}", e.getMessage());
        }
    }

    private static Dimension getScaledDimension(final int originalWidth, final int originalHeight) {
        float newWidth;
        float newHeight;
        if (originalWidth < originalHeight) {
            newWidth = PDRectangle.A4.getWidth();
            newHeight = (newWidth * originalHeight) / originalWidth;
        } else {
            newHeight = PDRectangle.A4.getHeight();
            newWidth = (newHeight * originalWidth) / originalHeight;
        }
        return new Dimension(new Float(newWidth).intValue(), new Float(newHeight).intValue());
    }
}
