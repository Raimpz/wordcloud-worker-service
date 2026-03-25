package com.wordcloud.worker.repository;

import com.wordcloud.worker.entity.WordCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WordCountRepository extends JpaRepository<WordCount, Integer> {
    Optional<WordCount> findByDocumentIdAndWord(String documentId, String word);
}
