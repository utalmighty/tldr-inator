package inc.huduk.tldr_inator.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.azure.AzureOpenAiStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import inc.huduk.tldr_inator.service.llm.Assistant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    @Value("${ollama.url}")
    String ollamaUrl;
    @Value("${ollama.model.name}")
    String ollamaModel;

    @Value("${azure.openai.api.endpoint}")
    String azureEndpoint;
    @Value("${azure.openai.api.key}")
    String azureKey;
    @Value("${azure.openai.api.version}")
    String azureVersion;


    @Bean
    EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2QuantizedEmbeddingModel();
    }

    @Bean
    InMemoryEmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }


    @Bean
    StreamingChatModel azure() {
        return AzureOpenAiStreamingChatModel.builder()
                .apiKey(azureKey)
                .endpoint(azureEndpoint)
                .serviceVersion(azureVersion)
                .build();
    }

    @Bean
    StreamingChatModel ollama() {
        return OllamaStreamingChatModel.builder()
                .baseUrl(ollamaUrl)
                .modelName(ollamaModel)
                .build();
    }

    @Bean
    Assistant assistant() {
        return AiServices.create(Assistant.class, azure());
    }
}
