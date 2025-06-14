package inc.huduk.tldr_inator.service.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class SentenceSplitter implements SplitterService {

    @Value("${splitter.segment.size}")
    int segmentSizeInChar;
    @Value("${splitter.segment.overlap}")
    int overlapSize;

    @Override
    public Flux<TextSegment> chunkDocument(Document doc) {
        DocumentBySentenceSplitter splitter = new DocumentBySentenceSplitter(segmentSizeInChar, overlapSize);
        var textSegments = splitter.split(doc);
        return Flux.fromIterable(textSegments);
    }
}
