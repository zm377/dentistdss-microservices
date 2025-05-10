package press.mizhifei.dentist.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Configuration class for Spring Cloud Gateway with WebFlux.
 * Provides additional configuration and customization for the gateway.
 */
@Configuration
@EnableWebFlux
public class GatewayConfig implements WebFluxConfigurer {

    /**
     * You can add custom beans and configuration methods here as needed.
     * For example, you might want to configure custom route locators, 
     * filters, or other WebFlux-specific components.
     */
}
