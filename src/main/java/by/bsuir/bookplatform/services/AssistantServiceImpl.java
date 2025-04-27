package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.ChatHistoryResponse;
import by.bsuir.bookplatform.DTO.ChatRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.github.GitHubModelsChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssistantServiceImpl implements AssistantService {

    private final GitHubModelsChatModel model;
    private final ChatMemoryProvider chatMemoryProvider;

    @Override
    public String getResponse(ChatRequest request) {

        String chatId = request.userId();
        var memory = chatMemoryProvider.get(chatId);

        String instructions = """
                Вы — сотрудник онлайн-чата службы поддержки клиентов книжного магазина "BookPlatform". Отвечайте дружелюбно, профессионально и с энтузиазмом, как будто вы рады помочь каждому клиенту. Вы общаетесь с покупателями через систему онлайн-чата. Сегодня {{current_date}}.
                """;

        memory.add(SystemMessage.systemMessage(instructions));
        memory.add(UserMessage.userMessage(request.question()));

        List<ChatMessage> messages = memory.messages();
        var aiResponse = model.chat(messages).aiMessage();

        String responseText = aiResponse.text();
        if (responseText.contains("ResponsibleAIPolicyViolation") || responseText.contains("content_filter")) {
            String errorString = "Ваш запрос нарушает правила использования интеллектуального ассистента.";
            AiMessage errorMessage = new AiMessage(errorString);
            memory.add(errorMessage);
            return errorString;
        }

        memory.add(aiResponse);
        return responseText;
    }

    @Override
    public ChatHistoryResponse getChatHistory(String userId) {
        String chatId = userId != null ? userId : "anonymous";
        var memory = chatMemoryProvider.get(chatId);

        List<ChatMessage> messages = memory.messages();
        List<ChatHistoryResponse.ChatMessageDTO> messageDTOs = messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new ChatHistoryResponse(messageDTOs);
    }

    private ChatHistoryResponse.ChatMessageDTO convertToDTO(ChatMessage message) {
        if (message instanceof SystemMessage) {
            return new ChatHistoryResponse.ChatMessageDTO("SYSTEM", message.text(), false);
        } else if (message instanceof UserMessage) {
            return new ChatHistoryResponse.ChatMessageDTO("USER", message.text(), true);
        } else if (message instanceof AiMessage) {
            return new ChatHistoryResponse.ChatMessageDTO("AI", message.text(), false);
        } else {
            return new ChatHistoryResponse.ChatMessageDTO("UNKNOWN", message.text(), false);
        }
    }
}
