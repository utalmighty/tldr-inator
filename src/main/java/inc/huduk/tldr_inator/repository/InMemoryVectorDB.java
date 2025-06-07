package inc.huduk.tldr_inator.repository;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import inc.huduk.tldr_inator.service.embedding.EmbeddingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Repository
@AllArgsConstructor
public class InMemoryVectorDB {

    InMemoryEmbeddingStore<TextSegment> inMemoryEmbeddingStore;
    EmbeddingService embeddingService;
    Map<String, List<TextSegment>> shortTermMemory = new HashMap<>();

    public Flux<TextSegment> search(String uuid, String query) {
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
                        .matches())
                .map(EmbeddingMatch::embedded);
    }

    public Mono<String> add(TextSegment segment) {
        String uuid = segment.metadata("uuid");
        shortTermMemory.computeIfAbsent(uuid, _ -> new ArrayList<>()).add(segment);
        return embeddingService.embed(segment).map(embedding -> inMemoryEmbeddingStore.add(embedding, segment));
    }

    public Flux<TextSegment> fetchFullContent(String uuid) {
        var list = shortTermMemory.get(uuid)
                .stream()
                .sorted(Comparator.comparingInt(a -> Integer.parseInt(a.metadata().get("index"))))
                .toList();
        shortTermMemory.remove(uuid); // clear
        return Flux.fromIterable(list);
    }
}
