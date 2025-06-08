package inc.huduk.tldr_inator.repository;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import inc.huduk.tldr_inator.service.embedding.EmbeddingService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Repository
@RequiredArgsConstructor
public class InMemoryVectorDB {

    @NonNull InMemoryEmbeddingStore<TextSegment> inMemoryEmbeddingStore;
    @NonNull EmbeddingService embeddingService;

    @Value("${context.results.size}")
    private int results;

    public Flux<TextSegment> search(String uuid, String query) {
        Filter filterByUUID = metadataKey("uuid").isEqualTo(uuid);

        return embeddingService.embed(query)
                .map(embedding ->
                        EmbeddingSearchRequest.builder()
                                .filter(filterByUUID)
                                .queryEmbedding(embedding)
                                .maxResults(results)
                                .build())
                .flatMapIterable(request -> inMemoryEmbeddingStore
                        .search(request)
                        .matches())
                .map(EmbeddingMatch::embedded);
    }

    public Mono<String> add(TextSegment segment) {
        return embeddingService.embed(segment).map(embedding -> inMemoryEmbeddingStore.add(embedding, segment));
    }
}
