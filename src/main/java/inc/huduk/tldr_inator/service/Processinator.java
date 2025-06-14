package inc.huduk.tldr_inator.service;

import dev.langchain4j.data.document.DefaultDocument;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import inc.huduk.tldr_inator.repository.ChatHistory;
import inc.huduk.tldr_inator.repository.InMemoryContentHolder;
import inc.huduk.tldr_inator.repository.InMemoryVectorDB;
import inc.huduk.tldr_inator.service.llm.Assistant;
import inc.huduk.tldr_inator.service.reader.PDFReader;
import inc.huduk.tldr_inator.service.splitter.SplitterService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class Processinator {

    @NonNull PDFReader pdfReader;
    @NonNull SplitterService splitter;
    @NonNull InMemoryVectorDB inMemoryVectorDB;
    @NonNull InMemoryContentHolder db;
    @NonNull ChatHistory history;
    @NonNull Assistant assistant;

    @Value("${llm.short.term.memory.system.message}")
    private String shortTermMemorySystemMessage;

    @Value("${llm.regular.chat.system.message}")
    private String chatSystemMessage;

    public Flux<String> chat(String sessionId, String query) {
        String chatHistory = history.history(sessionId);
        String finalQuery = String.format(chatSystemMessage, chatHistory, query);
        return assistant.chat(finalQuery);
    }

    /*
    * Save to short term memory
     */
    public Mono<String> addToShortTermMemory(FilePart filePart) {
        String uuid = UUID.randomUUID().toString();
        return pdfReader.fileContent(filePart)
                .doOnNext(content-> db.add(uuid, content))
                .map(content-> new DefaultDocument(content, Metadata.metadata("uuid", uuid)))
                .flatMapMany(doc-> splitter.chunkDocument(doc))
                .flatMap(chunk-> inMemoryVectorDB.add(chunk))
                .map(x-> uuid)
                .next();
    }

    public Flux<String> promptShortTermMemory(String sessionId, String documentId, String query) {
        String chatHistory = history.history(sessionId);
        return inMemoryVectorDB.search(documentId, query)
                .map(TextSegment::text)
                .collect(Collectors.joining("\n"))
                .map(context-> String.format(shortTermMemorySystemMessage, context, chatHistory, query))
                .flatMapMany(assistant::chat);
    }

    public Flux<String> clear(String sessionId, String documentId) {
        history.clear(sessionId);
        db.clear(documentId);
        inMemoryVectorDB.clear(documentId);
        return Flux.just("Cleared");
    }

    public Flux<String> summary(String uuid) {
        if (!db.contains(uuid)) return Flux.just("Document not found");
        return Mono.just(db.get(uuid))
                .map(TextSegment::from)
                .flatMapMany(this::summaryOfSegment);
    }

    private Flux<String> summaryOfSegment(TextSegment segment) {
        return assistant.chat(segment.text() + "\n Please summarize the above text segment");
    }

}
