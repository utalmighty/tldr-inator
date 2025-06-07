package inc.huduk.tldr_inator.service.llm;

import inc.huduk.tldr_inator.models.HudukRequest;
import inc.huduk.tldr_inator.models.ModelRequest;
import inc.huduk.tldr_inator.models.ModelResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
@Slf4j
public class Llama2Service implements LLMService {

    WebClient ollamaWebClient;

    public Flux<String> prompt(@NonNull final HudukRequest request) {
        ModelRequest modelRequest = ModelRequest.builder().model("llama2").prompt(request.prompt()).build();
        return ollamaWebClient.post()
                .bodyValue(modelRequest)
                .retrieve()
                .bodyToFlux(ModelResponse.class)
                .map(ModelResponse::response);
    }
}
