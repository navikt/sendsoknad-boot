package no.nav.sbl.pdfutility;

public class PdfUtilities {

    public static boolean isPng(byte[] bytes) {
        return FiletypeSjekker.isPng(bytes);
    }

    public static boolean isPDF(byte[] bytes) {
        return FiletypeSjekker.isPdf(bytes);
    }

    public static boolean isImage(byte[] bytes) {
        return FiletypeSjekker.isImage(bytes);
    }

    public static byte[] createPDFFromImage(byte[] image) {
        return KonverterTilPdf.createPDFFromImage(image);
    }

    public static byte[] konverterTilPng(byte[] in, int sideNr) {
        return KonverterTilPng.konverterTilPng(in, sideNr);
    }

    public static boolean erPDFA(byte[] input) {
        return PdfaSjekker.erPDFA(input);
    }

    public static void erGyldig(byte[] input) {
        PdfGyldighetsSjekker.erGyldig(input);
    }

    public static byte[] mergePdfer(Iterable<byte[]> docs) {
        return PdfMerger.mergePdfer(docs);
    }

    public static int finnAntallSider(byte[] bytes) {
        return PdfMerger.finnAntallSider(bytes);
    }
}
