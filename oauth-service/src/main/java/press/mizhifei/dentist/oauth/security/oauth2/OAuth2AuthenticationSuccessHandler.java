package press.mizhifei.dentist.oauth.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import press.mizhifei.dentist.oauth.security.JwtTokenProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhifeimi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;

    private List<String> authorizedRedirectUris= new ArrayList<>() {{
        add("https://dentist.mizhifei.press/oauth2/redirect");
        add("http://localhost:3000/oauth2/redirect");
    }};

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, authentication);

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to {}", targetUrl);
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, Authentication authentication) {
        String redirectUri = request.getParameter("redirect_uri");
        if (redirectUri != null && !isAuthorizedRedirectUri(redirectUri)) {
            throw new IllegalArgumentException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }

        // Use the first authorized redirect URI if none is provided
        String targetUrl = redirectUri != null ? redirectUri : authorizedRedirectUris.get(0);
        String token = tokenProvider.generateToken(authentication);

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token)
                .build().toUriString();
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        return authorizedRedirectUris.stream()
                .anyMatch(authorizedRedirectUri -> uri.startsWith(authorizedRedirectUri));
    }
} 