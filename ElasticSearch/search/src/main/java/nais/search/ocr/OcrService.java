package nais.search.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
public class OcrService {

    private final Tesseract tesseract;
    private final int maxPages;

    public OcrService(@Value("${ocr.tessdata-path:/usr/share/tesseract-ocr/5/tessdata}") String tessDataPath,
                       @Value("${ocr.language:eng}") String language,
                       @Value("${ocr.max-pages:20}") int maxPages) {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(tessDataPath);
        this.tesseract.setLanguage(language);
        this.maxPages = maxPages;
    }

    public String extractText(byte[] pdfBytes) throws IOException, TesseractException {
        StringBuilder text = new StringBuilder();
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pages = Math.min(document.getNumberOfPages(), maxPages);
            for (int page = 0; page < pages; page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, 150);
                text.append(tesseract.doOCR(image));
                text.append("\n");
            }
        }
        return text.toString().trim();
    }
}
