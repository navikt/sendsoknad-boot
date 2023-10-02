package no.nav.sbl.pdfutility;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessStreamCache;
import org.apache.pdfbox.io.RandomAccessStreamCache.StreamCacheCreateFunction;
import org.apache.pdfbox.pdmodel.DefaultResourceCache;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.imgscalr.Scalr;
import org.slf4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.lang.Double.max;
import static java.lang.Double.min;
import static java.lang.Math.round;
import static org.apache.pdfbox.io.MemoryUsageSetting.setupMainMemoryOnly;
import static org.slf4j.LoggerFactory.getLogger;

class KonverterTilPng {
    private static final Logger logger = getLogger(KonverterTilPng.class);

    static byte[] konverterTilPng(String behandlingsId, byte[] in, int sideNr) {
        if (in == null || in.length == 0) {
            logger.error("{}: Kan ikke konvertere en tom fil til PNG", behandlingsId);
            throw new RuntimeException("Kan ikke konvertere en tom fil til PNG");
        }
        byte[] png = fraPDFTilPng(behandlingsId, in, sideNr);
        logger.info("{}: Konvertert til Png. Filstørrelse={}", behandlingsId, png.length);
        return png;
    }

    private static StreamCacheCreateFunction getMemorySettings(int bytes)  {
        return setupMainMemoryOnly(bytes).streamCache;
    }

    /**
     * Konverterer en PDF til en liste av PNG filer
     *
     * @param in byte array av PDF
     * @return Liste av byte array av PNG
     */
    private static byte[] fraPDFTilPng(String behandlingsId, byte[] in, int side) {
            try (var pd = Loader.loadPDF(in, "", null, null, getMemorySettings(500 * 1024 * 1024))) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                pd.setResourceCache(new MyResourceCache());
                PDFRenderer pdfRenderer = new PDFRenderer(pd);

                int pageIndex = pd.getNumberOfPages() - 1 < side ? pd.getNumberOfPages() - 1 : Math.max(side, 0);
                BufferedImage bim = pdfRenderer.renderImageWithDPI(pageIndex, 100, ImageType.RGB);
                bim = scaleImage(bim, new Dimension(600, 800), true);

                ImageIOUtil.writeImage(bim, "PNG", byteArrayOutputStream, 300, 1.0f);
                bim.flush();
                return byteArrayOutputStream.toByteArray();
            }
         catch (IOException e) {
            logger.error("{}: Klarte ikke å konvertere pdf til png", behandlingsId, e);
            throw new RuntimeException("Klarte ikke å konvertere pdf til png");
        }
    }

    private static class MyResourceCache extends DefaultResourceCache {
        @Override
        public void put(COSObject indirect, PDXObject xobject) {
            // Hindrer caching ved å kommenterer ut default kall til super sin cache
            //super.put(indirect, xobject);
        }
    }

    private static BufferedImage scaleImage(BufferedImage image, Dimension boundingBox, boolean fitInsideBox) {
        double scaleFactorWidth = boundingBox.getWidth() / image.getWidth();
        double scaleFactorHeight = boundingBox.getHeight() / image.getHeight();

        double scalingFactor;
        if (fitInsideBox) {
            scalingFactor = min(scaleFactorWidth, scaleFactorHeight);
        } else {
            scalingFactor = max(scaleFactorWidth, scaleFactorHeight);
        }

        BufferedImage scaledImage = Scalr.resize(image, (int) (scalingFactor * image.getWidth()), (int) (scalingFactor * image.getHeight()));

        if (!fitInsideBox) {
            return cropImage(scaledImage, boundingBox);
        } else {
            return scaledImage;
        }
    }

    private static BufferedImage cropImage(BufferedImage image, Dimension boundingBox) {
        if (boundingBox.getWidth() > image.getWidth() || boundingBox.getHeight() > image.getHeight()) {
            throw new IllegalArgumentException("Bildet må være minst like stort som boksen.");
        }
        int newWidth = (int) round(boundingBox.getWidth());
        int newHeight = (int) round(boundingBox.getHeight());

        int widthDelta = image.getWidth() - newWidth;
        int heightDelta = image.getHeight() - newHeight;

        return image.getSubimage(widthDelta / 2, heightDelta / 2, newWidth, newHeight);
    }
}
