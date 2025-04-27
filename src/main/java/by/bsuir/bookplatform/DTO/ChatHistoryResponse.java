package by.bsuir.bookplatform.DTO;

import java.util.List;

public record ChatHistoryResponse(List<ChatMessageDTO> messages) {

    public record ChatMessageDTO(String type, String text, boolean isUser) {
    }
}