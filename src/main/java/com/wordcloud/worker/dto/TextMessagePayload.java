package com.wordcloud.worker.dto;

public class TextMessagePayload {
    private String documentId;
    private String textChunk;

    public TextMessagePayload() {}

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setTextChunk(String textChunk) {
        this.textChunk = textChunk;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getTextChunk() {
        return textChunk;
    }
}
