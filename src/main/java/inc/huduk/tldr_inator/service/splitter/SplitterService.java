package inc.huduk.tldr_inator.service.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import reactor.core.publisher.Flux;

public interface SplitterService {

    Flux<TextSegment> chunkDocument(Document doc);
}
