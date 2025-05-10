package press.mizhifei.dentist.oauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author zhifeimi
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class OAuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OAuthServiceApplication.class, args);
    }
} 