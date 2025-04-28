package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.ChatHistoryResponse;
import by.bsuir.bookplatform.DTO.ChatRequest;
import dev.ai4j.openai4j.OpenAiHttpException;
import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssistantServiceImpl implements AssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AssistantServiceImpl.class);
    @Qualifier("assistantService")
    private final AssistantService assistantService;
    private final ChatMemoryProvider chatMemoryProvider;

    private static final int MAX_TOKENS = 4500; // Запас до лимита 5000
    private static final int MAX_MESSAGES = 10; // Максимальное количество сообщений в истории

    @Override
    public String getResponse(ChatRequest request) {
        String chatId = request.userId();
        var memory = chatMemoryProvider.get(chatId);

        // Получаем текущую историю сообщений
        List<ChatMessage> currentMessages = memory.messages();
        logger.debug("Current chat memory before adding new message (chatId: {}): \n{}",
                chatId,
                currentMessages.stream()
                        .map(msg -> msg.getClass().getSimpleName() + ": " + msg.text())
                        .collect(Collectors.joining("\n")));

        // Добавляем пользовательское сообщение
        UserMessage userMessage = UserMessage.userMessage(request.question());
        memory.add(userMessage);
        logger.debug("Added UserMessage to memory: {}", userMessage.text());

        // Ограничиваем историю сообщений
        List<ChatMessage> trimmedMessages = trimChatHistory(memory.messages());
        memory.clear();
        trimmedMessages.forEach(memory::add);
        logger.debug("Trimmed chat history to {} messages, estimated tokens: {}",
                trimmedMessages.size(), estimateTokens(trimmedMessages));

        String responseText;
        try {
            // Логируем сообщения, отправляемые модели
            logger.info("Sending request to AiAssistantService with messages: \n{}",
                    trimmedMessages.stream()
                            .map(msg -> msg.getClass().getSimpleName() + ": " + msg.text())
                            .collect(Collectors.joining("\n")));

            responseText = assistantService.getResponse(request);
            logger.info("Received response: {}", responseText);

            // Проверяем, есть ли ToolExecutionResultMessage в памяти
            List<ChatMessage> messages = memory.messages();
            boolean hasToolResult = messages.stream().anyMatch(msg -> msg instanceof ToolExecutionResultMessage);
            if (messages.stream().anyMatch(msg -> msg instanceof AiMessage && ((AiMessage) msg).hasToolExecutionRequests())) {
                logger.info("Tool calls detected in memory");
                if (hasToolResult) {
                    logger.info("ToolExecutionResultMessage found in memory: {}",
                            messages.stream()
                                    .filter(msg -> msg instanceof ToolExecutionResultMessage)
                                    .map(msg -> ((ToolExecutionResultMessage) msg).text())
                                    .collect(Collectors.joining(", ")));
                } else {
                    logger.warn("ToolExecutionResultMessage missing in memory for chatId: {}", chatId);
                }
            }

        } catch (OpenAiHttpException e) {
            logger.error("API error: {}", e.getMessage(), e);
            String errorString;
            if (e.getMessage().contains("429")) {
                errorString = "Достигнут лимит запросов. Пожалуйста, попробуйте позже.";
            } else if (e.getMessage().contains("tool_call_id")) {
                errorString = "Ошибка обработки инструмента: результат инструмента не сохранён в истории. Пожалуйста, попробуйте снова.";
            } else if (e.getMessage().contains("Maximum number of tokens")) {
                errorString = "Превышен лимит токенов в запросе. Пожалуйста, сократите запрос или начните новый чат.";
            } else {
                errorString = "Ошибка при обращении к API: " + e.getMessage();
            }
            AiMessage errorMessage = new AiMessage(errorString);
            memory.add(errorMessage);
            return errorString;
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            String errorString = "Произошла внутренняя ошибка при обработке запроса. Пожалуйста, попробуйте позже.";
            AiMessage errorMessage = new AiMessage(errorString);
            memory.add(errorMessage);
            return errorString;
        }

        // Проверяем на нарушение политики
        if (responseText.contains("ResponsibleAIPolicyViolation") || responseText.contains("content_filter")) {
            String errorString = "Ваш запрос нарушает правила использования интеллектуального ассистента.";
            AiMessage errorMessage = new AiMessage(errorString);
            memory.add(errorMessage);
            return errorString;
        }

        // Добавляем ответ AI в память
        AiMessage aiMessage = new AiMessage(responseText);
        memory.add(aiMessage);
        logger.debug("Added AiMessage to memory: {}", responseText);

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
        } else if (message instanceof ToolExecutionResultMessage) {
            return new ChatHistoryResponse.ChatMessageDTO("TOOL", ((ToolExecutionResultMessage) message).text(), false);
        } else {
            return new ChatHistoryResponse.ChatMessageDTO("UNKNOWN", message.text(), false);
        }
    }

    private List<ChatMessage> trimChatHistory(List<ChatMessage> messages) {
        if (messages.size() <= MAX_MESSAGES && estimateTokens(messages) <= MAX_TOKENS) {
            return new ArrayList<>(messages);
        }

        List<ChatMessage> trimmed = new ArrayList<>();
        SystemMessage systemMessage = null;

        // Сохраняем системное сообщение, если оно есть
        for (ChatMessage msg : messages) {
            if (msg instanceof SystemMessage) {
                systemMessage = (SystemMessage) msg;
                break;
            }
        }

        // Добавляем системное сообщение, если оно существует
        if (systemMessage != null) {
            trimmed.add(systemMessage);
        }

        // Добавляем последние сообщения, пока не превысим MAX_MESSAGES или MAX_TOKENS
        int messagesToKeep = MAX_MESSAGES - (systemMessage != null ? 1 : 0);
        for (int i = Math.max(0, messages.size() - messagesToKeep); i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            if (!(msg instanceof SystemMessage)) { // Пропускаем системное сообщение, если оно уже добавлено
                trimmed.add(msg);
                if (estimateTokens(trimmed) > MAX_TOKENS) {
                    trimmed.remove(trimmed.size() - 1); // Удаляем последнее сообщение, если превышен лимит токенов
                    break;
                }
            }
        }

        logger.info("Trimmed chat history from {} to {} messages", messages.size(), trimmed.size());
        return trimmed;
    }

    private int estimateTokens(List<ChatMessage> messages) {
        int totalTokens = 0;
        for (ChatMessage msg : messages) {
            String text = msg.text();
            if (text != null) {
                // Приблизительный подсчёт: 1 токен ≈ 4 символа (с учётом кода и текста на русском)
                int tokens = text.length() / 4 + 1; // +1 для учёта пробелов и метаданных
                totalTokens += tokens;
            }
        }
        return totalTokens;
    }
}