package com.wordcloud.worker.listener;

import com.wordcloud.worker.dto.TextMessagePayload;
import com.wordcloud.worker.service.TextProcessingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageQueueListener {

    private final TextProcessingService textProcessingService;

    public MessageQueueListener(TextProcessingService textProcessingService) {
        this.textProcessingService = textProcessingService;
    }

    @RabbitListener(queues = "text-processing-queue")
    public void receiveMessage(TextMessagePayload payload) {
        try {
            textProcessingService.processPayload(payload);
        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
        }
    }
}
