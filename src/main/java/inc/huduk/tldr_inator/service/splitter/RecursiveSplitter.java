package inc.huduk.tldr_inator.service.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class RecursiveSplitter implements SplitterService {

    int segmentSizeInChar = 1500;
    int overlapSize = 0;

    @Override
    public Flux<TextSegment> chunkDocument(Document doc) {
        DocumentSplitter splitter = DocumentSplitters.recursive(segmentSizeInChar, overlapSize);
        var textSegments = splitter.split(doc);
        return Flux.fromIterable(textSegments);
    }
}
