package com.dentistdss.genai.service;

import com.dentistdss.genai.model.FAQ;
import com.dentistdss.genai.repository.FAQRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for FAQ operations and intelligent FAQ matching
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FAQService {
    
    private final FAQRepository faqRepository;
    
    /**
     * Search for relevant FAQs based on user input
     */
    public List<FAQ> searchRelevantFAQs(String userInput, Long clinicId) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        String cleanInput = userInput.toLowerCase().trim();
        
        // Try different search strategies
        List<FAQ> results = new ArrayList<>();
        
        // 1. Full-text search
        try {
            List<FAQ> textSearchResults = faqRepository.findByTextSearch(cleanInput);
            results.addAll(textSearchResults);
        } catch (Exception e) {
            log.warn("Text search failed: {}", e.getMessage());
        }
        
        // 2. Keyword-based search
        List<String> keywords = extractKeywords(cleanInput);
        if (!keywords.isEmpty()) {
            List<FAQ> keywordResults = faqRepository.findByKeywordsIn(keywords);
            results.addAll(keywordResults);
        }
        
        // 3. Category-based search
        String category = detectCategory(cleanInput);
        if (category != null) {
            List<FAQ> categoryResults = faqRepository.findByCategoryAndClinicIdOrGlobal(category, clinicId);
            results.addAll(categoryResults);
        }
        
        // Remove duplicates and sort by relevance
        return results.stream()
                .distinct()
                .sorted((faq1, faq2) -> {
                    // Sort by priority first, then by relevance score
                    int priorityCompare = Integer.compare(
                        faq2.getPriority() != null ? faq2.getPriority() : 0,
                        faq1.getPriority() != null ? faq1.getPriority() : 0
                    );
                    if (priorityCompare != 0) return priorityCompare;
                    
                    // Calculate relevance score
                    double score1 = calculateRelevanceScore(faq1, cleanInput);
                    double score2 = calculateRelevanceScore(faq2, cleanInput);
                    return Double.compare(score2, score1);
                })
                .limit(5) // Return top 5 most relevant FAQs
                .collect(Collectors.toList());
    }
    
    /**
     * Get context information for AI agent based on user input
     */
    public String getApiContextForAgent(String agent, String userInput, Long clinicId) {
        if (!"help".equalsIgnoreCase(agent)) {
            return null;
        }
        
        List<FAQ> relevantFAQs = searchRelevantFAQs(userInput, clinicId);
        
        if (relevantFAQs.isEmpty()) {
            return null;
        }
        
        StringBuilder context = new StringBuilder();
        context.append("Relevant FAQ Information:\n");
        
        for (FAQ faq : relevantFAQs) {
            context.append("Q: ").append(faq.getQuestion()).append("\n");
            context.append("A: ").append(faq.getAnswer()).append("\n\n");
        }
        
        return context.toString();
    }
    
    /**
     * Extract keywords from user input
     */
    private List<String> extractKeywords(String input) {
        // Simple keyword extraction - can be enhanced with NLP libraries
        String[] words = input.split("\\s+");
        List<String> keywords = new ArrayList<>();
        
        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            if (word.length() > 2 && !isStopWord(word)) {
                keywords.add(word);
            }
        }
        
        return keywords;
    }
    
    /**
     * Detect category from user input
     */
    private String detectCategory(String input) {
        Map<String, List<String>> categoryKeywords = Map.of(
            "APPOINTMENTS", Arrays.asList("appointment", "booking", "schedule", "reschedule", "cancel"),
            "CLINIC_HOURS", Arrays.asList("hours", "open", "closed", "time", "when"),
            "SERVICES", Arrays.asList("service", "treatment", "procedure", "cleaning", "filling"),
            "BILLING", Arrays.asList("bill", "payment", "cost", "price", "fee", "charge"),
            "INSURANCE", Arrays.asList("insurance", "coverage", "claim", "copay"),
            "EMERGENCY", Arrays.asList("emergency", "urgent", "pain", "after hours"),
            "AFTERCARE", Arrays.asList("aftercare", "recovery", "healing", "post-treatment")
        );
        
        for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (input.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Calculate relevance score for FAQ
     */
    private double calculateRelevanceScore(FAQ faq, String userInput) {
        double score = 0.0;
        
        // Check question similarity
        if (faq.getQuestion().toLowerCase().contains(userInput)) {
            score += 3.0;
        }
        
        // Check answer similarity
        if (faq.getAnswer().toLowerCase().contains(userInput)) {
            score += 2.0;
        }
        
        // Check keyword matches
        if (faq.getKeywords() != null) {
            List<String> inputKeywords = extractKeywords(userInput);
            long matchingKeywords = faq.getKeywords().stream()
                    .mapToLong(keyword -> inputKeywords.contains(keyword.toLowerCase()) ? 1 : 0)
                    .sum();
            score += matchingKeywords * 1.5;
        }
        
        // Boost score based on popularity
        score += (faq.getViewCount() != null ? faq.getViewCount() : 0) * 0.01;
        score += (faq.getHelpfulCount() != null ? faq.getHelpfulCount() : 0) * 0.1;
        
        return score;
    }
    
    /**
     * Check if word is a stop word
     */
    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
            "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did",
            "will", "would", "could", "should", "may", "might", "can", "this", "that", "these", "those"
        );
        return stopWords.contains(word);
    }
}
