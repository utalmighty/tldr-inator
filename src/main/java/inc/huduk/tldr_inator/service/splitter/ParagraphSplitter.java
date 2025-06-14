package inc.huduk.tldr_inator.service.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Primary
@Service
@Slf4j
public class ParagraphSplitter implements SplitterService {

    @Value("${splitter.segment.size}")
    int segmentSizeInChar;
    @Value("${splitter.segment.overlap}")
    int overlapSize;

    @Override
    public Flux<TextSegment> chunkDocument(Document doc) {
        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(segmentSizeInChar, overlapSize);
        var textSegments = splitter.split(doc);
        return Flux.fromIterable(textSegments);
    }
}
