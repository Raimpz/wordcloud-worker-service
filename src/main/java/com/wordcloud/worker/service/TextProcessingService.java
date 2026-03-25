package com.wordcloud.worker.service;

import com.wordcloud.worker.dto.TextMessagePayload;
import com.wordcloud.worker.entity.WordCount;
import com.wordcloud.worker.repository.WordCountRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.Optional;

@Service
public class TextProcessingService {

    private final WordCountRepository wordCountRepository;

    private final Set<String> STOP_WORDS = Set.of("and", "or", "the", "a", "an", "is", "in", "to", "of", "it");

    public TextProcessingService(WordCountRepository wordCountRepository) {
        this.wordCountRepository = wordCountRepository;
    }

    public void processPayload(TextMessagePayload payload) {
        boolean isTextEmpty = payload.getTextChunk() == null || payload.getTextChunk().trim().isEmpty();

        if (isTextEmpty) {
            return;
        }

        String[] words = payload.getTextChunk().toLowerCase().split("[^a-zA-Z]+");

        for (String word : words) {
            boolean shouldSkip = word.isEmpty() || STOP_WORDS.contains(word);

            if (shouldSkip) {
                continue;
            }

            Optional<WordCount> existingWordCount = wordCountRepository.findByDocumentIdAndWord(payload.getDocumentId(), word);

            if (existingWordCount.isPresent()) {
                WordCount wordCount = existingWordCount.get();
                wordCount.setCount(wordCount.getCount() + 1);

                wordCountRepository.save(wordCount);
            } else {
                WordCount newWordCount = new WordCount();
                newWordCount.setDocumentId(payload.getDocumentId());
                newWordCount.setWord(word);
                newWordCount.setCount(1);

                wordCountRepository.save(newWordCount);
            }
        }
    }
}
