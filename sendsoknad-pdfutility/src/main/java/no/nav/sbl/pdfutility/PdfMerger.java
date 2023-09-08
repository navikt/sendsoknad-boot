package no.nav.sbl.pdfutility;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

class PdfMerger {

    private static final Logger logger = getLogger(PdfMerger.class);

    private static byte[] mergePdfer(List<RandomAccessRead> docs) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDFMergerUtility merger = new PDFMergerUtility();
            merger.setDestinationStream(out);
            merger.addSources(docs);
            merger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly().streamCache);
            return ((ByteArrayOutputStream) merger.getDestinationStream()).toByteArray();
        } catch (IOException e) {
            logger.error("Merge av PDF dokumenter feilet");
            throw new RuntimeException("Merge av PDF dokumenter feilet");
        }
    }

    static byte[] mergePdfer(Iterable<byte[]> docs) {
        List<RandomAccessRead> is = new ArrayList<>();
        try {
            for (byte[] bytes : docs) {
                is.add(new RandomAccessReadBuffer(bytes));
            }
            return mergePdfer(is);
        } finally {
            is.forEach(i -> {
                try {
                    i.close();
                } catch (IOException e) {
                    logger.error("Opprydding etter merging av PDFer feilet");
                }
            });
        }
    }

    static int finnAntallSider(byte[] bytes) {
        try (var document = Loader.loadPDF(bytes)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            logger.error("Klarer ikke å åpne PDF for å kunne skjekke antall sider");
            throw new RuntimeException("Klarer ikke å åpne PDF for å kunne skjekke antall sider");
        }
    }
}
