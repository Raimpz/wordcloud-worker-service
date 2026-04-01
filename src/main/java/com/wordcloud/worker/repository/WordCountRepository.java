package com.wordcloud.worker.repository;

import com.wordcloud.worker.entity.WordCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WordCountRepository extends JpaRepository<WordCount, Integer> {

    @Modifying
    @Query(value = """
            INSERT INTO word_counts (document_id, word, count)
            VALUES (:documentId, :word, :count)
            ON CONFLICT (document_id, word)
            DO UPDATE SET count = word_counts.count + :count
            """, nativeQuery = true)
    void upsertWordCount(
        @Param("documentId") String documentId,
        @Param("word") String word,
        @Param("count") int count
    );
}
