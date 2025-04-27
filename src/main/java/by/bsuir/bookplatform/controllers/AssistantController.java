package by.bsuir.bookplatform.controllers;

import by.bsuir.bookplatform.DTO.ChatRequest;
import by.bsuir.bookplatform.DTO.ChatResponse;
import by.bsuir.bookplatform.services.AssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/chat")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AssistantController {

    private final AssistantService assistantService;

    @PostMapping
    public ChatResponse getChatResponse(@RequestBody ChatRequest request) {
        return new ChatResponse(assistantService.getResponse(request));
    }
}
