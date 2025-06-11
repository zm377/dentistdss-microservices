package press.mizhifei.dentist.chatlog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token usage tracking for AI interactions
 * Embedded document for detailed token consumption metrics
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsage {
    
    /**
     * Number of tokens in the user input
     */
    private Integer inputTokens;
    
    /**
     * Number of tokens in the AI response
     */
    private Integer outputTokens;
    
    /**
     * Total tokens consumed (input + output)
     */
    private Integer totalTokens;
    
    /**
     * Estimated cost in USD (if available)
     */
    private Double estimatedCost;
    
    /**
     * AI model used for the interaction
     */
    private String model;
    
    /**
     * Response time in milliseconds
     */
    private Long responseTimeMs;
}
