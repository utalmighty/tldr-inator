package inc.huduk.tldr_inator.service;

import inc.huduk.tldr_inator.repository.ChatHistory;
import lombok.AllArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class Interceptor {

    ChatHistory history;
    Processinator processinator;

    public Flux<String> chat(String sessionId, String query) {
        Flux<String> flux = processinator.chat(sessionId, query);
        flux.reduce("", (acc, item)-> acc + item).doOnSuccess(resp-> history.add(sessionId, resp)).subscribe();
        return flux;
    }

    public Flux<String> promptShortTermMemory(String sessionId, String documentId, String query) {
        Flux<String> flux = processinator.promptShortTermMemory(sessionId, documentId, query);
        flux.reduce("", (acc, item)-> acc + item).doOnSuccess(resp-> history.add(sessionId, resp)).subscribe();
        return flux;
    }

    public Flux<String> summary(String sessionId, String documentId) {
        Flux<String> flux = processinator.summary(documentId);
        flux.reduce("", (acc, item)-> acc + item).doOnSuccess(resp-> history.add(sessionId, resp)).subscribe();
        return flux;
    }

    public Mono<String> addToShortTermMemory(String sesssionId, FilePart filePart) {
        return processinator.addToShortTermMemory(filePart);
    }

}
