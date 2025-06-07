package inc.huduk.tldr_inator.service.embedding;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class AllMiniLM implements EmbeddingService {

    // all-minilm:latest  46MB 512 Text
    EmbeddingModel model;

    @Override
    public Mono<Embedding> embed(String s) {
        return embed(TextSegment.textSegment(s));
    }

    @Override
    public Mono<Embedding> embed(TextSegment segment) {
        return embedAll(List.of(segment)).last();
    }

    @Override
    public Flux<Embedding> embedAll(List<TextSegment> segments) {
        return Flux.fromIterable(model.embedAll(segments).content());
    }
}
