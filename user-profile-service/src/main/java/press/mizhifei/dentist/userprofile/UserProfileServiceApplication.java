package press.mizhifei.dentist.userprofile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * User Profile Service Application
 * Manages both patient and dentist profiles
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class UserProfileServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserProfileServiceApplication.class, args);
    }
}