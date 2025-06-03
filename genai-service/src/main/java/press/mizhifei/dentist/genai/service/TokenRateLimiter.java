package press.mizhifei.dentist.genai.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Component
public class TokenRateLimiter {

    private static final long MAX_TOKENS = 10_000;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(3);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(String sessionId, long tokens) {
        Bucket bucket = buckets.computeIfAbsent(sessionId, id -> Bucket.builder()
                .addLimit(Bandwidth.classic(MAX_TOKENS, Refill.greedy(MAX_TOKENS, REFILL_DURATION)))
                .build());
        return bucket.tryConsume(tokens);
    }
} 