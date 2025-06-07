package inc.huduk.tldr_inator.controller;

import inc.huduk.tldr_inator.models.HudukRequest;
import inc.huduk.tldr_inator.models.HudukResponse;
import inc.huduk.tldr_inator.service.Processinator;
import inc.huduk.tldr_inator.service.llm.LLMService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@RestController
@AllArgsConstructor
@CrossOrigin("*")
@Slf4j
public class TLDR_InatorController {

    LLMService llmService;
    Processinator processinator;

    @GetMapping("/")
    Mono<HudukResponse> status() {
        return Mono.just(new HudukResponse("tl;dr-inator is running"));
    }

    @GetMapping(value = "/tldr", produces = "text/event-stream")
    Flux<HudukResponse> prompt(@RequestParam String prompt) {
        return llmService.prompt(new HudukRequest(prompt)).map(HudukResponse::new);
    }

    @PostMapping(value = "/short-term-memory")
    Mono<HudukResponse> uploadPdf(@RequestPart("file") FilePart file) {
        log.info("HERE");
        return processinator.addToShortTermMemory(file);
    }

    @GetMapping(value = "/short-term-memory", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<HudukResponse> promptShortTermMemory(@RequestParam String query) {
        return processinator.promptShortTermMemory(query).map(HudukResponse::new);
    }

    @GetMapping(value = "/summarize", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<HudukResponse> summary() {
        return processinator.summary().map(HudukResponse::new);
    }
}

