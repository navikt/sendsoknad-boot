package no.nav.sbl.pdfutility;

import org.apache.pdfbox.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static no.nav.sbl.pdfutility.FiletypeSjekker.isImage;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FiletypeSjekkerTest {

    @Test
    public void sjekkAtJPgErImage() throws IOException {
        assertTrue(isImage(getBytesFromFile("/images/skog.jpg")));
    }

    @Test
    public void sjekkAtPngErImage() throws IOException {
        assertTrue(isImage(getBytesFromFile("/images/edderkopp.png")));
    }

    @Test
    public void sjekkAtTiffAvvisesSomImage() throws IOException {
        assertFalse(isImage(getBytesFromFile("/images/edderkopp.tif")));
    }

    @Test
    public void sjekkAtBMPAvvisesSomImage() throws IOException {
        assertFalse(isImage(getBytesFromFile("/images/edderkopp.bmp")));
    }

    @Test
    public void sjekkAtGIFAvvisesSomImage() throws IOException {
        assertFalse(isImage(getBytesFromFile("/images/edderkopp.gif")));
    }

    private static byte[] getBytesFromFile(String path) throws IOException {
        InputStream resourceAsStream = FilHjelpUtility.class.getResourceAsStream(path);
        return IOUtils.toByteArray(resourceAsStream);
    }
}