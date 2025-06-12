package inc.huduk.tldr_inator.service.llm;

import reactor.core.publisher.Flux;

public interface Assistant {
        Flux<String> chat(String s);
}
