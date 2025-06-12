package inc.huduk.tldr_inator.service;

import inc.huduk.tldr_inator.models.HudukResponse;
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
        StringBuffer buffer = new StringBuffer();
        return processinator.chat(sessionId, query).map(resp-> {
            buffer.append(resp);
            return resp;
        })
        .doOnComplete(()-> history.add(sessionId, buffer.toString()));
    }

    public Flux<String> promptShortTermMemory(String sessionId, String documentId, String query) {
        StringBuffer buffer = new StringBuffer();
        return processinator.promptShortTermMemory(sessionId, documentId, query).map(resp-> {
                    buffer.append(resp);
                    return resp;
                })
                .doOnComplete(()-> history.add(sessionId, buffer.toString()));
    }

    public Flux<String> summary(String sessionId, String documentId) {
        StringBuffer buffer = new StringBuffer();
        return processinator.summary(documentId).map(resp-> {
                    buffer.append(resp);
                    return resp;
                })
                .doOnComplete(()-> history.add(sessionId, buffer.toString()));
    }

    public Mono<HudukResponse> addToShortTermMemory(String sesssionId, FilePart filePart) {
        return processinator.addToShortTermMemory(filePart).map(HudukResponse::new);
    }

}
