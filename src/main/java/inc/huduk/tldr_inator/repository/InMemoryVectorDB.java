package inc.huduk.tldr_inator.repository;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import inc.huduk.tldr_inator.service.embedding.EmbeddingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@AllArgsConstructor
public class InMemoryVectorDB {

    InMemoryEmbeddingStore<TextSegment> inMemoryEmbeddingStore;
    EmbeddingService embeddingService;

    public Flux<EmbeddingMatch<TextSegment>> search(String query) {
        return embeddingService.embed(query)
                .map(embedding ->
                        EmbeddingSearchRequest.builder()
                                .queryEmbedding(embedding)
                                .maxResults(2)
                                .build())
                .flatMapIterable(request -> inMemoryEmbeddingStore
                        .search(request)
                        .matches());
    }

    public Mono<String> add(String input) {
        TextSegment segment = TextSegment.from(input);
        return embeddingService.embed(segment).map(embedding -> inMemoryEmbeddingStore.add(embedding, segment));
    }

    public Flux<EmbeddingMatch<TextSegment>> fetchFullPdfContent() {
        return embeddingService.embed("")
                .map(embedding ->
                        EmbeddingSearchRequest.builder()
                                .queryEmbedding(embedding)
                                .maxResults(15)
                                .build())
                .flatMapIterable(request -> inMemoryEmbeddingStore
                        .search(request)
                        .matches());
    }
}
