package by.bsuir.bookplatform.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.github.GitHubModelsChatModel;
import dev.langchain4j.model.github.GitHubModelsChatModelName;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String GITHUB_TOKEN = dotenv.get("GITHUB_TOKEN");

    @Bean
    public GitHubModelsChatModel gitHubModelsChatModel() {
        return GitHubModelsChatModel.builder()
                .gitHubToken(GITHUB_TOKEN)
                .modelName(GitHubModelsChatModelName.GPT_4_O)
                .logRequestsAndResponses(true)
                .temperature(0d)
                .build();
    }

    @Bean
    public Tokenizer tokenizer() {
        return new OpenAiTokenizer("gpt-4o");
    }

    @Bean
    public ChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    @Bean
    ChatMemoryProvider chatMemoryProvider(Tokenizer tokenizer, ChatMemoryStore chatMemoryStore) {
        return chatId -> TokenWindowChatMemory.builder()
                .id(chatId) // Устанавливаем chatId
                .maxTokens(2000, tokenizer)
                .chatMemoryStore(chatMemoryStore)
                .build();
    }
}