package com.wordcloud.worker.service;

import com.wordcloud.worker.dto.TextMessagePayload;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class TextProcessingService {

    private static final int MAX_ALLOWED_WORD_LENGTH = 255;

    private final WordCountBatchService wordCountBatchService;

    private final Set<String> IGNORED_COMMON_WORDS = Set.of("and", "or", "the", "a", "an", "is", "in", "to", "of", "it");

    public TextProcessingService(WordCountBatchService wordCountBatchService) {
        this.wordCountBatchService = wordCountBatchService;
    }

    public void processPayload(TextMessagePayload payload) {
        boolean isTextEmpty = payload.getTextChunk() == null || payload.getTextChunk().trim().isEmpty();

        if (isTextEmpty) {
            wordCountBatchService.batchUpsertAndTrack(payload.getDocumentId(), Map.of());

            return;
        }

        String[] words = payload.getTextChunk().toLowerCase().split("[^a-zA-Z0-9]+");

        Map<String, Integer> wordCounts = new HashMap<>();

        for (String word : words) {
            boolean shouldSkip = word.isEmpty() || IGNORED_COMMON_WORDS.contains(word) || (word.matches("\\d+"));

            if (shouldSkip) {
                continue;
            }

            String truncatedWord = word.length() > MAX_ALLOWED_WORD_LENGTH ? word.substring(0, MAX_ALLOWED_WORD_LENGTH) : word;
            wordCounts.merge(truncatedWord, 1, Integer::sum);
        }

        wordCountBatchService.batchUpsertAndTrack(payload.getDocumentId(), wordCounts);
    }
}
