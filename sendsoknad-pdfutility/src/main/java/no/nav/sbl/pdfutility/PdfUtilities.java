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

    public static byte[] konverterTilPng(String behandlingsId, byte[] in, int sideNr) {
        return KonverterTilPng.konverterTilPng(behandlingsId, in, sideNr);
    }

    public static boolean erPDFA(String behandlingsId, byte[] input) {
        return PdfaSjekker.erPDFA(behandlingsId, input);
    }

    public static void erGyldig(String behandlingsId, byte[] input) {
        PdfGyldighetsSjekker.erGyldig(behandlingsId, input);
    }

    public static byte[] mergePdfer(Iterable<byte[]> docs) {
        return PdfMerger.mergePdfer(docs);
    }

    public static int finnAntallSider(byte[] bytes) {
        return PdfMerger.finnAntallSider(bytes);
    }
}
