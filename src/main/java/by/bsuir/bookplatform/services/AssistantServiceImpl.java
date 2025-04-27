package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.ChatRequest;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.github.GitHubModelsChatModel;
import dev.langchain4j.model.github.GitHubModelsChatModelName;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssistantServiceImpl implements AssistantService {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String GITHUB_TOKEN = dotenv.get("GITHUB_TOKEN");

    @Override
    public String getResponse(ChatRequest request) {
        List<ChatMessage> messages = new ArrayList<>();

        String instructions = "Вы — сотрудник онлайн-чата службы поддержки клиентов книжного магазина \"BookPlatform\". Отвечайте дружелюбно, полезно и с радостью. Вы общаетесь с покупателями через систему онлайн-чата. Перед тем как предоставить информацию о заказе или отменить заказ, ОБЯЗАТЕЛЬНО убедитесь, что у пользователя есть следующие данные: номер заказа, имя, фамилия. Проверьте историю переписки, прежде чем запрашивать эти данные повторно. После оформления заказ изменять НЕЛЬЗЯ. Можно отменить заказ, но для этого ОБЯЗАТЕЛЬНО запрашивай у пользователя дополнительное подтверждение. Если пользователь просит порекомендовать книгу, обязательно уточните его интересы, предпочтения по жанру, любимых авторов или книги, которые ему понравились ранее, а также информацию о книгах в наличии. Основывайтесь на этих данных, чтобы предложить релевантные книги. Предлагайте с воодушевлением, как будто делитесь личными любимыми находками, но, тем не менее сдержанно и соблюдая официальный стиль общения. Используйте предоставленные функции для: рекомендации книг, получения информации о заказе, отмены заказа. Сегодня {{current_date}}";

        messages.add(SystemMessage.systemMessage(instructions));

        messages.add(UserMessage.userMessage(request.question()));

        GitHubModelsChatModel model = GitHubModelsChatModel.builder().gitHubToken(GITHUB_TOKEN).modelName(GitHubModelsChatModelName.GPT_4_O).logRequestsAndResponses(true).temperature(0d).build();

        return model.chat(messages).aiMessage().text();
    }
}
