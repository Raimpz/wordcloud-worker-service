package com.wordcloud.worker.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WordCountBatchService {

    private static final int BATCH_SIZE = 500;

    private final JdbcTemplate jdbcTemplate;

    public WordCountBatchService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void batchUpsertAndTrack(String documentId, Map<String, Integer> wordCounts) {
        if (!wordCounts.isEmpty()) {
            List<Map.Entry<String, Integer>> wordCountEntries = new ArrayList<>(wordCounts.entrySet());

            for (int batchStartIndex = 0; batchStartIndex < wordCountEntries.size(); batchStartIndex += BATCH_SIZE) {
                List<Map.Entry<String, Integer>> currentBatch = wordCountEntries.subList(batchStartIndex, Math.min(batchStartIndex + BATCH_SIZE, wordCountEntries.size()));

                executeBatchUpsert(documentId, currentBatch);
            }
        }

        jdbcTemplate.update("""
            UPDATE documents
            SET processed_chunks = processed_chunks + 1,
                status = CASE
                    WHEN processed_chunks + 1 >= total_chunks AND total_chunks > 0 THEN 'COMPLETED'
                    ELSE status
                END
            WHERE id = ?
            """, documentId);
    }

    private void executeBatchUpsert(String documentId, List<Map.Entry<String, Integer>> batch) {
        StringBuilder sqlQuery = new StringBuilder("INSERT INTO word_counts (document_id, word, count) VALUES ");
        List<Object> queryParameters = new ArrayList<>();
        boolean isFirstEntry = true;

        for (Map.Entry<String, Integer> entry : batch) {
            if (!isFirstEntry) sqlQuery.append(", ");

            sqlQuery.append("(?, ?, ?)");
            queryParameters.add(documentId);
            queryParameters.add(entry.getKey());
            queryParameters.add(entry.getValue());
            isFirstEntry = false;
        }

        sqlQuery.append(" ON CONFLICT (document_id, word) DO UPDATE SET count = word_counts.count + EXCLUDED.count");

        jdbcTemplate.update(sqlQuery.toString(), queryParameters.toArray());
    }
}
