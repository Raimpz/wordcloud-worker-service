package com.wordcloud.worker.service;

import com.wordcloud.worker.dto.TextMessagePayload;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class TextProcessingService {

    private static final int MAX_ALLOWED_WORD_LENGTH = 50;

    private final WordCountBatchService wordCountBatchService;

    private static final Set<String> IGNORED_COMMON_WORDS = Set.of(
            // English
            "a", "an", "and", "are", "as", "at", "be", "been", "but", "by",
            "can", "did", "do", "does", "for", "from", "had", "has", "have",
            "he", "if", "in", "is", "it", "its", "me", "not", "of", "on",
            "or", "she", "so", "that", "the", "they", "this", "to", "was",
            "we", "were", "will", "with", "would", "you",
            // Estonian
            "aga", "ei", "et", "ja", "ka", "kes", "kuid", "kui", "kõik",
            "ma", "mis", "mitte", "nad", "need", "nii", "ning", "ole", "oli",
            "oma", "või", "sa", "see", "seda", "sest", "siis", "ta", "te",
            "veel", "väga"
    );

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
            boolean isOnlyNumbers = (word.matches("\\d+"));
            boolean shouldSkip = word.isEmpty() || word.length() == 1 || IGNORED_COMMON_WORDS.contains(word) || isOnlyNumbers;

            if (shouldSkip) {
                continue;
            }

            String truncatedWord = word.length() > MAX_ALLOWED_WORD_LENGTH ? word.substring(0, MAX_ALLOWED_WORD_LENGTH) : word;
            wordCounts.merge(truncatedWord, 1, Integer::sum);
        }

        wordCountBatchService.batchUpsertAndTrack(payload.getDocumentId(), wordCounts);
    }
}
