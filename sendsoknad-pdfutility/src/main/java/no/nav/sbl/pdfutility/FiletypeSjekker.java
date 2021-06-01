package no.nav.sbl.pdfutility;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.tika.Tika;

import java.util.function.Predicate;

class FiletypeSjekker {

    static final Predicate<byte[]> IS_PNG = bytes ->
            (new Tika()).detect(ArrayUtils.subarray(bytes.clone(), 0, 2048)).equalsIgnoreCase("image/png");
    static final Predicate<byte[]> IS_PDF = bytes ->
            (new Tika()).detect(bytes).equalsIgnoreCase("application/pdf");
    static final Predicate<byte[]> IS_JPG = bytes ->
            (new Tika()).detect(bytes).equalsIgnoreCase("image/jpeg");
    static final Predicate<byte[]> IS_IMAGE = bytes -> IS_PNG.test(bytes) || IS_JPG.test(bytes);

    static boolean isPng(byte[] bytes) {
        return IS_PNG.test(bytes);
    }

    static boolean isPdf(byte[] bytes) {
        return IS_PDF.test(bytes);
    }

    static boolean isJpg(byte[] bytes) {
        return IS_JPG.test(bytes);
    }

    static boolean isImage(byte[] bytes) {
        return IS_IMAGE.test(bytes);
    }
}
