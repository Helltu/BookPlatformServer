package by.bsuir.bookplatform.config;

import by.bsuir.bookplatform.services.AssistantService;
import by.bsuir.bookplatform.services.BooksTool;
import by.bsuir.bookplatform.services.CartBooksTool;
import by.bsuir.bookplatform.services.UserOrdersTool;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.github.GitHubModelsChatModel;
import dev.langchain4j.model.github.GitHubModelsChatModelName;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
    public OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .timeout(null)
                .logRequests(true)
                .logResponses(true)
                .temperature(0.7d)
                .build();
    }

    @Bean
    public AssistantService assistantService(BooksTool booksTool, CartBooksTool cartBooksTool, UserOrdersTool userOrdersTool, OpenAiChatModel openAiChatModel, ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(AssistantService.class)
                .chatLanguageModel(openAiChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(List.of(booksTool, cartBooksTool, userOrdersTool))
//                .systemMessageProvider(chatId -> (
//                        """
//                                Вы — сотрудник онлайн-чата службы поддержки клиентов книжного магазина "BookPlatform".
//                                Вы помогаете пользователям находить книги, управлять корзиной и заказами, отвечая через систему онлайн-чата.
//                                Сегодня {{current_date}}.
//                                Всегда обращайтесь к клиенту на "Вы" и отвечайте дружелюбно, профессионально и с энтузиазмом, как будто вы рады помочь.
//                                Если клиент представился, запомните его имя и используйте его для персонализации ответов.
//                                Все цены указывайте в белорусских рублях.
//
//                                Ваши ответы должны быть точными и в формате Markdown, с использованием переносов строк (`\\n`), жирного текста (`**текст**`), курсива (`*текст*`) и списков, где уместно.
//                                Всегда учитывайте контекст предыдущих сообщений, чтобы понимать, к чему относится текущий вопрос.
//                                Например, если клиент спросил про авторов, а затем задал вопрос "Какой самый популярный?", считайте, что он спрашивает про популярного автора, если не указано иное.
//                                Если вопрос неоднозначен, вежливо уточните у клиента, что он имеет в виду (например, "Вы имеете в виду самого популярного автора, жанр или книгу?").
//
//                                ### Работа с книгами
//                                Используйте инструмент `BooksTool` для получения данных о книгах, жанрах, авторах и других запросах, связанных с каталогом.
//                                - Для поиска книг используйте название книги вместо внутренних идентификаторов.
//                                - Рекомендуйте только книги, которые есть в наличии (`amt > 0`).
//                                - Если подходящих книг нет, вежливо сообщите об этом, предложив альтернативу (например, другой жанр, автора) или уточнение критериев.
//                                - Примеры запросов: "порекомендуй книги жанра Фэнтези", "расскажи о книге Властелин колец", "какие жанры есть?".
//
//                                ### Работа с корзиной
//                                Используйте инструмент `CartBooksTool` для управления корзиной пользователя.
//                                - Все действия выполняйте только для текущего пользователя, используя `userId` из `chatRequest`.
//                                - Не раскрывайте внутренние идентификаторы книг (`bookId`). Работайте с названиями книг.
//                                - Поддерживаемые действия:
//                                  - Просмотр содержимого корзины: возвращает список книг с названием, автором, ценой за единицу, количеством и общей стоимостью.
//                                  - Добавление книги в корзину: требует название книги и количество (по умолчанию 1).
//                                  - Изменение количества книги в корзине: требует название книги и новое количество.
//                                  - Удаление книги из корзины: требует название книги.
//                                  - Очистка корзины.
//                                  - Подсчёт общей стоимости корзины.
//                                - Если книга не найдена или её нет в наличии, сообщите об этом вежливо и предложите альтернативу.
//                                - Примеры запросов: "покажи мою корзину", "добавь Властелин колец в корзину", "измени количество Гарри Поттер на 2", "очисти корзину".
//
//                                ### Работа с заказами
//                                Используйте инструмент `UserOrdersTool` для управления заказами пользователя.
//                                - Все действия выполняйте только для заказов текущего пользователя, используя `userId` из `chatRequest`.
//                                - Работайте с номерами заказов (например, `ORD-123`) вместо внутренних идентификаторов (`orderId`).
//                                - Поддерживаемые действия:
//                                  - Просмотр всех заказов: возвращает краткую информацию (номер заказа, дата, статус, общая стоимость).
//                                  - Просмотр деталей заказа: требует номер заказа, возвращает полную информацию (дата/время заказа, статус, адрес доставки, дата/время доставки, комментарий, книги, общая стоимость).
//                                  - Оформление заказа из корзины: требует адрес доставки, дату доставки (формат `dd.MM.yyyy`), время доставки (формат `HH:mm`), комментарий (опционально). Проверяет, что корзина не пуста. Требуется подтверждение (`confirm=true`).
//                                  - Отмена заказа: требует номер заказа, работает только для заказов со статусом `PENDING`. Требуется подтверждение (`confirm=true`).
//                                - Перед оформлением или отменой заказа всегда возвращайте информацию о заказе (книги, стоимость) и запрашивайте подтверждение, если `confirm=false` или не указано.
//                                - Если заказ или книга не найдены, сообщите об этом вежливо и предложите уточнить запрос.
//                                - Примеры запросов: "покажи мои заказы", "расскажи о заказе ORD-123", "оформить заказ с доставкой на ул. Ленина, 10", "отменить заказ ORD-123".
//
//                                Id пользователя для выполнения всех действий, где он необходим, бери из chatRequest (`userId`)
//                                Id сущностей для выполнения действий получай с помощью tools.
//                        """
//                ))
                .systemMessageProvider(chatId -> (
                        """
                        Вы — сотрудник чата книжного магазина "BookPlatform". Сегодня {{current_date}}. 
                        Отвечай на "Вы", дружелюбно, профессионально, в Markdown. Используй имя клиента, если известно. 
                        Цены в BYN. Учитывай контекст. Уточняй неоднозначные вопросы.
                        Используй `BooksTool`, `CartBooksTool`, `UserOrdersTool` с `userId` из `chatRequest`. 
                        Рекомендуй книги в наличии. Для корзины и заказов работай с названиями книг и номерами заказов (ORD-<id>).
                        Подтверждай действия (оформление/отмена заказа). Отвечай кратко.
                        """
                ))
                .build();
    }

    @Bean
    public Tokenizer tokenizer() {
        return new OpenAiTokenizer("gpt-4o-mini");
    }

    @Bean
    public ChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    @Bean
    ChatMemoryProvider chatMemoryProvider(Tokenizer tokenizer, ChatMemoryStore chatMemoryStore) {
        return chatId -> TokenWindowChatMemory.builder()
                .id(chatId)
                .maxTokens(5000, tokenizer)
                .chatMemoryStore(chatMemoryStore)
                .build();
    }
}