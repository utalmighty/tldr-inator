package inc.huduk.tldr_inator.repository;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.filter.Filter;
import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

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

    public Flux<EmbeddingMatch<TextSegment>> search(String uuid, String query) {
        Filter filterByUUID = metadataKey("uuid").isEqualTo(uuid);

        return embeddingService.embed(query)
                .map(embedding ->
                        EmbeddingSearchRequest.builder()
                                .filter(filterByUUID)
                                .queryEmbedding(embedding)
                                .maxResults(2)
                                .build())
                .flatMapIterable(request -> inMemoryEmbeddingStore
                        .search(request)
                        .matches());
    }

    public Mono<String> add(TextSegment segment) {
        return embeddingService.embed(segment).map(embedding -> inMemoryEmbeddingStore.add(embedding, segment));
    }

    public Flux<EmbeddingMatch<TextSegment>> fetchFullContent(String uuid) {
        Filter filterByUUID = metadataKey("uuid").isEqualTo(uuid);
        var request = EmbeddingSearchRequest.builder().filter(filterByUUID).build();
        var list = inMemoryEmbeddingStore.search(request).matches();
        return Flux.fromIterable(list);

    }
}
