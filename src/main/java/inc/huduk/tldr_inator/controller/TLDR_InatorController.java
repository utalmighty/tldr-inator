package inc.huduk.tldr_inator.controller;

import inc.huduk.tldr_inator.models.HudukResponse;
import inc.huduk.tldr_inator.service.Interceptor;
import inc.huduk.tldr_inator.service.llm.Assistant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@RestController
@AllArgsConstructor
@CrossOrigin("*")
@Slf4j
@RequestMapping("tldr/api")
public class TLDR_InatorController {

    Interceptor interceptor;
    Assistant assistant;

    @GetMapping
    Mono<HudukResponse> status() {
        return Mono.just(new HudukResponse("tl;dr-inator is running"));
    }

    @GetMapping("session/create")
    Mono<HudukResponse> session() {
        return Mono.just(new HudukResponse(UUID.randomUUID().toString()));
    }

    @GetMapping(value = "session/{sessionId}/chat", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<HudukResponse> chat(@PathVariable String sessionId, @RequestParam String query) {
        return interceptor.chat(sessionId, query).map(HudukResponse::new);
    }

    @PostMapping(value = "session/{sessionId}/short-term-memory")
    Mono<HudukResponse> uploadPdf(@PathVariable String sessionId, @RequestPart("file") FilePart file) {
        return interceptor.addToShortTermMemory(sessionId, file);
    }

    @GetMapping(value = "session/{sessionId}/short-term-memory/{uuid}", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<HudukResponse> promptShortTermMemory(@PathVariable String sessionId,
                                              @PathVariable String uuid,
                                              @RequestParam String query) {
        return interceptor.promptShortTermMemory(sessionId, uuid, query).map(HudukResponse::new);
    }

    @GetMapping(value = "session/{sessionId}/summarize/{uuid}", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<HudukResponse> summary(@PathVariable String sessionId, @PathVariable String uuid) {
        return interceptor.summary(sessionId, uuid).map(HudukResponse::new);
    }

    @GetMapping(value = "session/{sessionId}/command/{command}", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<HudukResponse> command(@PathVariable String sessionId, @PathVariable String command, @RequestParam String documentId) {
        return interceptor.command(sessionId, documentId, command).map(HudukResponse::new);
    }
}

