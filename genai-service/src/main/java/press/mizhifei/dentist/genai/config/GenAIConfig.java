package press.mizhifei.dentist.genai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Configuration
public class GenAIConfig {

    /**
     * Spring AI auto-configures a prototype {@link ChatClient.Builder} bean when the
     * `spring-ai-starter-model-openai` starter is on the classpath. We simply use
     * that builder to create a singleton {@link ChatClient} that the rest of the
     * application can inject.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
} 