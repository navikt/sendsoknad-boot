package no.nav.sbl.pdfutility;

import org.apache.pdfbox.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FilHjelpUtility {

    public static byte[] getBytesFromFile(String path) throws IOException {
        InputStream resourceAsStream = FilHjelpUtility.class.getResourceAsStream(path);
        return IOUtils.toByteArray(resourceAsStream);
    }

    public static void skrivTilDisk(String sti, byte[] bytes)throws IOException {
        try (FileOutputStream stream = new FileOutputStream(sti)) {
            stream.write(bytes);
        }
    }

}
