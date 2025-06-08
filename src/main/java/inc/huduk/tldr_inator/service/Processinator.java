package inc.huduk.tldr_inator.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import inc.huduk.tldr_inator.models.HudukRequest;
import inc.huduk.tldr_inator.repository.ChatHistory;
import inc.huduk.tldr_inator.repository.InMemoryContentHolder;
import inc.huduk.tldr_inator.repository.InMemoryVectorDB;
import inc.huduk.tldr_inator.service.llm.LLMService;
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
    @NonNull LLMService llm;
    @NonNull InMemoryContentHolder db;
    @NonNull ChatHistory history;

    @Value("${llm.short.term.memory.system.message}")
    private String shortTermMemorySystemMessage;

    @Value("${llm.regular.chat.system.message}")
    private String chatSystemMessage;

    public Flux<String> chat(String sessionId, String query) {
        String chatHistory = history.history(sessionId);
        String finalQuery = String.format(chatSystemMessage, chatHistory, query);
        return llm.prompt(new HudukRequest(finalQuery));
    }

    /*
    * Save to short term memory
     */
    public Mono<String> addToShortTermMemory(FilePart filePart) {
        String uuid = UUID.randomUUID().toString();
        return pdfReader.fileContent(filePart)
                .doOnNext(content-> db.add(uuid, content))
                .map(content-> new Document(content, Metadata.metadata("uuid", uuid)))
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
                .map(HudukRequest::new)
                .flatMapMany(llm::prompt);
    }

    public Flux<String> summary(String uuid) {
        return Mono.just(db.get(uuid))
                .map(TextSegment::from)
                .flatMapMany(this::summaryOfSegment);
    }

    private Flux<String> summaryOfSegment(TextSegment segment) {
        return llm.prompt(new HudukRequest(segment.text() + "\n Please summarize the above text segment"));
    }

}
