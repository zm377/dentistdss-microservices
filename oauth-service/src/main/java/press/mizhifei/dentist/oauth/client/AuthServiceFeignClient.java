package press.mizhifei.dentist.oauth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import press.mizhifei.dentist.oauth.dto.ApiResponse;
import press.mizhifei.dentist.oauth.dto.AuthResponse;
import press.mizhifei.dentist.oauth.dto.OAuthLoginRequest;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
// The name "auth-service" should match the spring.application.name of the auth-service
// and how it's registered with Eureka.
// The URL property can be used for local testing if not using Eureka for Feign.
@FeignClient(name = "auth-service", url = "${app.auth-service.url:http://auth-service}")
public interface AuthServiceFeignClient {

    @PostMapping("/auth/oauth/process") // This new endpoint needs to be created in AuthService
    ResponseEntity<ApiResponse<AuthResponse>> processOAuthLogin(@RequestBody OAuthLoginRequest oAuthLoginRequest);

} 