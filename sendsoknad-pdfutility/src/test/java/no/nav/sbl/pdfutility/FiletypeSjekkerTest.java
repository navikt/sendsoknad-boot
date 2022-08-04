package no.nav.sbl.pdfutility;

import org.junit.Test;

import java.io.IOException;

import static no.nav.sbl.pdfutility.FilHjelpUtility.getBytesFromFile;
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
}
