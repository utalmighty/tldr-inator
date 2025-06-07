package inc.huduk.tldr_inator.service.embedding;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EmbeddingService {

    Mono<Embedding> embed(String s);

    Mono<Embedding> embed(TextSegment segment);

    Flux<Embedding> embedAll(List<TextSegment> segments);
}
