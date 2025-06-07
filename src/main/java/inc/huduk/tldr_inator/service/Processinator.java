package inc.huduk.tldr_inator.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import inc.huduk.tldr_inator.models.HudukRequest;
import inc.huduk.tldr_inator.models.HudukResponse;
import inc.huduk.tldr_inator.repository.InMemoryVectorDB;
import inc.huduk.tldr_inator.service.llm.LLMService;
import inc.huduk.tldr_inator.service.reader.PDFReader;
import inc.huduk.tldr_inator.service.splitter.SplitterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class Processinator {

    PDFReader pdfReader;
    SplitterService splitter;
    InMemoryVectorDB inMemoryVectorDB;
    LLMService llm;

    static final String SYSTEM_MESSAGE = "You are an expert text summarizer. You'll receive a prompt that includes retrieved" +
            " content from the vectorDB based on the user's question, and the source. Your task is to respond to the user's " +
            "new question using the information from the vectorDB without relying on your own knowledge. \n Context:\n";

    static final String USER_QUERY_HEADING = "\n# User asked Query:\n";

    /*
    * Save to short term memory
     */
    public Mono<String> addToShortTermMemory(FilePart filePart) {
        String uuid = UUID.randomUUID().toString();
        return pdfReader.fileContent(filePart)
                .flatMapMany(content-> splitter.chunkDocument(new Document(content, Metadata.metadata("uuid", uuid))))
                .flatMap(chunk-> inMemoryVectorDB.add(chunk))
                .map(x-> uuid)
                .next();
    }

    public Flux<String> promptShortTermMemory(String uuid, String query) {
        return inMemoryVectorDB.search(uuid, query)
                .map(emb-> emb.embedded().text())
                .collect(Collectors.joining("\n"))
                .map(context-> SYSTEM_MESSAGE + context + USER_QUERY_HEADING + query)
                .map(x-> {
                    log.info(x);
                    return x;
                })
                .map(HudukRequest::new)
                .flatMapMany(llm::prompt);
    }

    public Flux<String> summary(String uuid) {
        return inMemoryVectorDB.fetchFullContent(uuid)
                .concatMap(matched-> summaryOfSegment(matched.embedded()))
                .collect(Collectors.joining("\n"))
                // do summary of summary
                .map(TextSegment::from)
                .flatMapMany(this::summaryOfSegment);
    }

    private Flux<String> summaryOfSegment(TextSegment segment) {
        return llm.prompt(new HudukRequest(segment.text() + "\n Please summarize the above text segment"));
    }


}
