package inc.huduk.tldr_inator.service.llm;

import inc.huduk.tldr_inator.models.HudukRequest;
import lombok.NonNull;
import reactor.core.publisher.Flux;

public interface LLMService {

    Flux<String> prompt(@NonNull final HudukRequest request);
}
