package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.ChatRequest;

public interface AssistantService {
    String getResponse(ChatRequest request);
}
