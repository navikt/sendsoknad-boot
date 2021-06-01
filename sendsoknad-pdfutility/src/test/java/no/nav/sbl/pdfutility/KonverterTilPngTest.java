package no.nav.sbl.pdfutility;

import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;

import static no.nav.sbl.pdfutility.FiletypeSjekker.isImage;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

public class KonverterTilPngTest {

    private static final Boolean skrivTilDisk = false;

    private static Logger logger = getLogger(KonverterTilPngTest.class);

    private void konverterTilPng(String filnavn) throws IOException {
        konverterTilPng(filnavn, 0);
    }

    private void konverterTilPng(String filnavn, int side) throws IOException {
        byte[] pdf = FilHjelpUtility.getBytesFromFile("/pdfs/"+filnavn+".pdf");
        long start = System.currentTimeMillis();
        long minneStart = Runtime.getRuntime().totalMemory();
        byte[] image =  KonverterTilPng.konverterTilPng(pdf, side);
        long minneEnd = Runtime.getRuntime().totalMemory();
        long end = System.currentTimeMillis();
        assertNotNull(image);
        assertTrue(isImage(image));

        logger.debug("Minnebruk={}",minneEnd);
        if (skrivTilDisk) {
            System.out.println("Endring minnebruk=" + (minneEnd-minneStart));
            System.out.println("Tidsbruk=" + (end-start));
            FilHjelpUtility.skrivTilDisk("c:/temp/delme-"+filnavn+"_"+side+".png", image);
        }
    }

    @Test
    public void createPDFFromImage_loop() throws IOException {
        String filnavn = "kafka-guide";
        logger.debug("Max tilgjengelig minne={}",Runtime.getRuntime().maxMemory());
        long max = Runtime.getRuntime().totalMemory();
        long minneStart = Runtime.getRuntime().totalMemory();
        for (int j=0; j < 1; j++) {
            for (int i = 0; i <= 5; i++) {
                konverterTilPng(filnavn, i);
                max = Math.max(max,Runtime.getRuntime().totalMemory());
            }
        }
        logger.debug("Max samplet minnebruk={}",max);
        assertTrue(max < 5*1024*1024*1024L);

        if (skrivTilDisk) {
            long minneEnd = Runtime.getRuntime().totalMemory();
            System.out.println("Endring i minnebruk=" + (minneEnd - minneStart));
            System.out.println("Max samplet minnebruk=" + max);
        }
    }

    @Test
    public void createPDFFromImage_formatProblem() throws IOException {
        String filnavn = "ceh";
        konverterTilPng(filnavn);
    }

    @Test
    public void createPDFFromImage_endringsbeskyttet() throws IOException {
        String filnavn = "endringsbeskyttet";
        konverterTilPng(filnavn);
    }

    @Test
    public void createPDFFromImage_minimal() throws IOException {
        String filnavn = "minimal";
        konverterTilPng(filnavn);
    }

    @Test
    public void createPDFFromImage_navskjema() throws IOException {
        String filnavn = "navskjema";
        konverterTilPng(filnavn);
    }

    @Test
    public void createPDFFromImage_scannet() throws IOException {
        String filnavn = "SCN_0004";
        konverterTilPng(filnavn);
    }

    @Test
    public void createPDFFromImage_stor_flere_sider() throws IOException {
        String filnavn = "stor";
        long max = Runtime.getRuntime().totalMemory();
        for (int i = 0; i<10; i++) {
            konverterTilPng(filnavn, i);
            max = Math.max(max,Runtime.getRuntime().totalMemory());
        }
        logger.debug("Max samplet minnebruk={}",max);
        assertTrue(max < 5*1024*1024*1024L);
    }

}
