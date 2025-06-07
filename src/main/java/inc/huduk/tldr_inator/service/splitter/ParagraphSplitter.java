package inc.huduk.tldr_inator.service.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Primary
@Service
@Slf4j
public class ParagraphSplitter implements SplitterService {

    int segmentSizeInChar = 1500;
    int overlapSize = 0;

    @Override
    public Flux<TextSegment> chunkDocument(Document doc) {
        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(segmentSizeInChar, overlapSize);
        var textSegments = splitter.split(doc);
        return Flux.fromIterable(textSegments)
                .index()
                .map(tuple-> {
                    tuple.getT2().metadata().put("index", tuple.getT1());
                    return tuple.getT2();
        });
    }
}
