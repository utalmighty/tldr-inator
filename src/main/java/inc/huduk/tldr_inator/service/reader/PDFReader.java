package inc.huduk.tldr_inator.service.reader;


import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
@Slf4j
public class PDFReader {

    public Mono<String> fileContent(FilePart file) {
        return Mono.just(file).flatMap(filePart ->
                    filePart.content()
                            .reduce(new ByteArrayOutputStream(), (out, buffer) -> {
                                try {
                                    byte[] bytes = new byte[buffer.readableByteCount()];
                                    buffer.read(bytes);
                                    out.write(bytes);
                                } catch (Exception e) {
                                    log.error("Something went wrong reading the pdf file. {}", e.getMessage());
                                }
                                return out;
                            })
                            .map(out -> {
                                try (InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
                                     PDDocument document = PDDocument.load(inputStream)) {

                                    PDFTextStripper stripper = new PDFTextStripper();
                                    return stripper.getText(document);

                                } catch (Exception e) {
                                    return "Failed to read PDF: " + e.getMessage();
                                }
                            })
        );
    }
}
